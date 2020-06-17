package net;

import ui.server.ServerFrame;
import util.Card;
import util.Deck;
import util.Suit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.UUID;

import static net.Constants.ERR_INVALID_MSG;
import static net.Constants.ERR_TOO_MANY_PLAYERS;
import static net.ServerToClientMessage.*;

public final class ModifiedNewServer extends ModifiedServer {
    private final LinkedHashMap<String, ModifiedClient> clients = new LinkedHashMap<>(4);
    private final String[] IDS = new String[4];

    public ModifiedNewServer(EventReceiver parent) {
        super(parent, Constants.PORT);
        System.out.println("Server started at: " + ModifiedServer.ip());
        ChatMessageHandler cmh = new ChatMessageHandler();
        cmh.start();
    }

    // MODIFIES: this
    // EFFECTS: resets the server (i.e. clears all remaining messages and writes out a reset message)
    public void reset() {
        write(createResetMessage());
    }

    // EFFECTS: determines whether there is not enough space for other players
    public boolean isFull() {
        return firstEmptySpace() == -1;
    }

    // MODIFIES: this
    // EFFECTS: runs when the game has started
    public void onGameStart(Deck[] decks) {
        if (decks.length != 4) throw new IllegalArgumentException();
        for (int i = 0; i < 4; i++) {
            sendNthClientMessage(i + 1, createStartGameMessage(decks[i]));
        }

    }

    // MODIFIES: this
    // EFFECTS: informs all the clients that the first turn is starting
    public void startFirstTurn(int starter, Deck[] hands, Deck[] newCards) {
        if (starter < 1 || starter > 4) throw new IllegalArgumentException("Invalid argument: " + starter);
        if (hands.length != 4) throw new IllegalArgumentException();
        sendNthClientMessage(1, createStartFirstTurnMessage(newCards[0], starter));
        sendNthClientMessage(2, createStartFirstTurnMessage(newCards[1], starter));
        sendNthClientMessage(3, createStartFirstTurnMessage(newCards[2], starter));
        sendNthClientMessage(4, createStartFirstTurnMessage(newCards[3], starter));

    }

    // MODIFIES: this
    // EFFECTS: informs players that a card has been played and requests the next card
    public void requestNextCard(int justPlayed, int nextPlayerNum, Deck center, Card played, Suit required) {
        write(createRequestNextCardMessage(played, justPlayed, nextPlayerNum, required));
    }

    // MODIFIES: this
    // EFFECTS: informs players that a round has ended and requests the "winner" (or loser) plays next
    public void startNewTurn(int winner, Deck addedPenalties) {
        write(createStartNextTurnMessage(winner, addedPenalties));
    }

    // MODIFIES: this
    // EFFECTS: informs players that the game has ended
    public void endGame(boolean[] winners, int points, Deck[] penaltyHands) {
        write(createGameEndMessage(winners, penaltyHands));
    }

    // MODIFIES: this
    // EFFECTS: runs when a new client connects
    public void onClientConnect(ModifiedClient c) {
        System.out.println("New client " + c.ip() + " connected to server  " + ModifiedServer.ip());
        int spot = firstEmptySpace();
        if (spot == -1) {
            c.write(createKickMessage(ERR_TOO_MANY_PLAYERS).toByteArr());
            disconnect(c);
        } else {
            String id = UUID.randomUUID().toString();
            // get existing players
            boolean[] existing = new boolean[4];
            for (int i = 0; i < IDS.length; i++) {
                if (IDS[i] != null && spot != i) {
                    existing[i] = true;
                }
            }
            c.write(createIDMessage(id, (spot + 1), existing).toByteArr());
            clients.put(id, c);
            IDS[spot] = id;
            informPlayersPlayerJoined(spot + 1);
            System.out.println(clients.size());
        }
    }

    // MODIFIES: this
    // EFFECTS: runs when a client disconnects
    public void onClientDisconnect(ModifiedClient c) {
        System.out.println("Client disconnected: " + c.ip());
        informPlayersOnDisconnect(c);
        removeClientFromEntries(c);
    }

    // EFFECTS: messages all other clients that a player has joined
    private void informPlayersPlayerJoined(int playerNumber) {
        ServerToClientMessage scm = createConnectionMessage(playerNumber);
        for (int i = 0; i < IDS.length; i++) {
            if (IDS[i] != null && i != playerNumber - 1) {
                sendNthClientMessage(i + 1, scm);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: informs other players that a player has disconnected
    private void informPlayersOnDisconnect(ModifiedClient c) {
        int playerNum = getClientNumber(c);
        if (playerNum == 0) return;
        write(createDisconnectMessage(playerNum));
    }

    // EFFECTS: returns client number (1-4), 0 if non-existent
    public int getClientNumber(ModifiedClient c) {
        if (!clients.containsValue(c)) return 0;
        for (String id : clients.keySet()) {
            if (clients.get(id).equals(c)) {
                if (IDS[0] != null && IDS[0].equals(id)) return 1;
                if (IDS[1] != null && IDS[1].equals(id)) return 2;
                if (IDS[2] != null && IDS[2].equals(id)) return 3;
                if (IDS[3] != null && IDS[3].equals(id)) return 4;
                else return 0;
            }
        }
        return 0;
    }

    // MODIFIES: this
    // EFFECTS: handles this chat message
    private void handleChatMessage(ClientToServerMessage csm, ModifiedClient sender) {
        if (csm == null) throw new IllegalArgumentException("csm cannot be null");
        if (!csm.isChatMessage()) throw new IllegalArgumentException("Called handleChatMessage on non-chat message");
        ServerToClientMessage scm = ServerToClientMessage.createChatMessage(csm.getChatMessage(), getClientNumber(sender));
        write(scm);
    }

    // MODIFIES: this
    // EFFECTS: kicks the client of given number (1-4)
    public void kickInvalid(int playerNum) {
        kick(clients.get(IDS[playerNum - 1]), ERR_INVALID_MSG);
    }

    // MODIFIES: this
    // EFFECTS: kicks the client with given message
    public void kick(ModifiedClient c, String msg) {
        c.write(ServerToClientMessage.createKickMessage(msg).toByteArr());
        disconnect(c);
        System.out.println("Client kicked: " + c.ip());
        removeClientFromEntries(c);
    }

    // MODIFIES: this
    // EFFECTS: writes out a ServerToClientMessage to clients
    public void write(ServerToClientMessage msg) {
        write(msg.toByteArr());
    }

    // EFFECTS: reads a ClientToServer message from a client
    //    NOTE: THIS WILL COMPLETELY FREEZE EXECUTION UNTIL THIS THING RECEIVES THE WHOLE MESSAGE
    private ClientToServerMessage readClientToServerMessage(ModifiedClient c) {
        while (c.available() < 4) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        int length = readInt(c);
        if (length < 0) throw new RuntimeException("NEGATIVE LENGTH OF MESSAGE?");
        byte[] msgBuffer = new byte[length];
        int bytesRead = 0;
        while (bytesRead < msgBuffer.length) {
            byte[] arr = c.readBytes(msgBuffer.length - bytesRead);
            System.arraycopy(arr, 0, msgBuffer, bytesRead, arr.length);
            bytesRead += arr.length;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(msgBuffer);
        try (ObjectInputStream in = new ObjectInputStream(bis)) {
            return (ClientToServerMessage) in.readObject();
        } catch (ClassNotFoundException | InvalidClassException | ClassCastException e) {
            kickInvalid(getClientNumber(c));
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // MODIFIES: c
    // EFFECTS: reads 4 bytes from a client and interprets it as an integer
    private int readInt(ModifiedClient c) {
        byte[] bytes = c.readBytes(4);
        return ByteBuffer.wrap(bytes).getInt();
    }

    // MODIFIES: this
    // EFFECTS: removes a client from the list of entries
    private void removeClientFromEntries(ModifiedClient c) {
        if (!clients.containsValue(c)) return;
        String toRemove = null;
        for (String s : clients.keySet()) {
            if (clients.get(s).equals(c)) {
                toRemove = s;
                for (int i = 0; i < 4; i++) {
                    if (toRemove.equals(IDS[i])) {
                        IDS[i] = null;
                        break;
                    }
                }
                break;
            }
        }
        assert (toRemove != null);
        clients.remove(toRemove);
        System.out.println("Successfully removed id " + toRemove + ", ip: " + c.ip());
    }

    // MODIFIES: this
    // EFFECTS: sends Nth client a message
    private void sendNthClientMessage(int n, ServerToClientMessage scm) {
        if (IDS[n - 1] == null) return;
        clients.get(IDS[n - 1]).write(scm.toByteArr());
    }

    // EFFECTS: returns the first empty index (0-3) for IDs
    private int firstEmptySpace() {
        if (IDS[0] == null) return 0;
        if (IDS[1] == null) return 1;
        if (IDS[2] == null) return 2;
        if (IDS[3] == null) return 3;
        return -1;
    }

    // Represents the thread that handles the chat messages
    private class ChatMessageHandler extends Thread {
        private boolean stop = false;

        // MODIFIES: this
        // EFFECTS: stops the thread
        public void end() {
            stop = true;
        }

        @Override
        // MODIFIES: server
        // EFFECTS: deals with chat messages by writing to clients and/or adding non-chat messages to server clientMessages
        public void run() {
            while (!stop) {
                ModifiedClient c = available();
                while (c != null) {
                    System.out.print("MESSAGE IS BEING RECEIVED.");
                    ClientToServerMessage csm = readClientToServerMessage(c);
                    if (csm == null) continue;
                    System.out.print(csm);
                    if (!csm.isValidMessage()) {
                        kickInvalid(getClientNumber(c));
                    } else if (csm.isChatMessage()) {
                        handleChatMessage(csm, c);
                        System.out.print("handled chat message.");
                    } else {
                        ServerFrame.getInstance().addNewMessage(csm);
                    }
                    c = available(); // get next client
                }
                delay(20); // runs 50 times a second
            }
        }

        // MODIFIES: this
        // EFFECTS: sleeps for ms milliseconds
        private void delay(long ms) {
            try {
                sleep(ms);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
