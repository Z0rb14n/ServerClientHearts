package net;

import net.message.NetworkMessage;
import net.message.client.ClientChatMessage;
import net.message.client.ClientToServerMessage;
import net.message.server.*;
import org.jetbrains.annotations.Contract;
import server.ui.Main;
import util.card.Card;
import util.card.Deck;
import util.card.Suit;

import java.io.IOException;
import java.io.InvalidClassException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.UUID;

import static net.Constants.*;

public final class ModifiedNewServer extends ModifiedServer implements EventReceiver {
    private final LinkedHashMap<String, ModifiedClient> clients = new LinkedHashMap<>(4);
    private final ChatMessageHandler cmh = new ChatMessageHandler();
    private final String[] IDS = new String[4];

    public ModifiedNewServer() {
        super(Constants.PORT);
        this.eventReceiver = this;
        System.out.println("Server started at: " + ModifiedServer.ip());
        cmh.start();
    }

    // MODIFIES: this
    // EFFECTS: resets the server (i.e. clears all remaining messages and writes out a reset message)
    public void reset() {
        try {
            write(new ServerGameResetMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            sendNthClientMessage(i + 1, new ServerStartGameMessage(decks[i]));
        }

    }

    // MODIFIES: this
    // EFFECTS: informs all the clients that the first turn is starting
    public void startFirstTurn(int starter, Deck[] hands, Deck[] newCards) {
        if (starter < 1 || starter > 4) throw new IllegalArgumentException("Invalid argument: " + starter);
        if (hands.length != 4) throw new IllegalArgumentException();
        sendNthClientMessage(1, new ServerStartFirstTurnMessage(newCards[0], starter));
        sendNthClientMessage(2, new ServerStartFirstTurnMessage(newCards[1], starter));
        sendNthClientMessage(3, new ServerStartFirstTurnMessage(newCards[2], starter));
        sendNthClientMessage(4, new ServerStartFirstTurnMessage(newCards[3], starter));

    }

    // MODIFIES: this
    // EFFECTS: informs players that a card has been played and requests the next card
    public void requestNextCard(int justPlayed, int nextPlayerNum, Deck center, Card played, Suit required) {
        try {
            write(new ServerRequestNextCardMessage(played, justPlayed, required, nextPlayerNum));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // MODIFIES: this
    // EFFECTS: informs players that a round has ended and requests the "winner" (or loser) plays next
    public void startNewTurn(int winner, Deck addedPenalties) {
        try {
            write(new ServerNextTurnMessage(addedPenalties, winner));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // MODIFIES: this
    // EFFECTS: informs players that the game has ended
    public void endGame(boolean[] winners, int[] points, Deck[] penaltyHands) {
        try {
            write(new ServerGameEndMessage(penaltyHands, winners));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Runs when a client connects to the server
     *
     * @param s Server in question
     * @param c client that connected
     */
    @Override
    @Contract(mutates = "this")
    public void clientConnectionEvent(ModifiedServer s, ModifiedClient c) {
        assert (s == this);
        System.out.println("New client " + c.ip() + " connected to server  " + ModifiedServer.ip());
        int spot = firstEmptySpace();
        if (spot == -1) {
            try {
                c.write(NetworkMessage.packetToByteArray(new ServerKickMessage(ERR_TOO_MANY_PLAYERS)));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            try {
                c.write(NetworkMessage.packetToByteArray(new ServerIDMessage(id, (spot + 1), existing)));
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    c.write(NetworkMessage.packetToByteArray(new ServerKickMessage(ERR_KICKED)));
                } catch (IOException ex) {
                    e.printStackTrace();
                } finally {
                    disconnect(c);
                }
            }
            clients.put(id, c);
            IDS[spot] = id;
            informPlayersPlayerJoined(spot + 1);
            System.out.println(clients.size());
        }
    }

    /**
     * Runs when a client has disconnected - remove a client from the entry list
     *
     * @param c client that has just disconnected
     */
    @Override
    @Contract(mutates = "this")
    public void disconnectEvent(ModifiedClient c) {
        if (clients.containsValue(c)) {
            System.out.println("Client disconnected: " + c.ip());
            informPlayersOnDisconnect(c);
            removeClientFromEntries(c);
        }
    }

    /**
     * Messages all other clients that a player has joined
     *
     * @param playerNumber player number that joined
     */
    private void informPlayersPlayerJoined(int playerNumber) {
        ServerToClientMessage scm = new ServerPlayerConnectionMessage(playerNumber);
        for (int i = 0; i < IDS.length; i++) {
            if (IDS[i] != null && i != playerNumber - 1) {
                System.out.println("Informing player " + (i + 1));
                sendNthClientMessage(i + 1, scm);
            }
        }
    }

    /**
     * Informs other players that a player has disconnected
     *
     * @param c Player that disconnected
     */
    @Contract(mutates = "this")
    private void informPlayersOnDisconnect(ModifiedClient c) {
        int playerNum = getClientNumber(c);
        if (playerNum == 0) return;
        ServerPlayerDisconnectionMessage message = new ServerPlayerDisconnectionMessage(playerNum);
        for (int i = 0; i < IDS.length; i++) {
            if (IDS[i] != null && i != playerNum - 1) {
                sendNthClientMessage(i + 1, message);
            }
        }
        System.out.println("Written out disconnection messages.");
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
    private void handleChatMessage(ClientChatMessage csm, ModifiedClient sender) {
        if (csm == null) throw new IllegalArgumentException("csm cannot be null");
        try {
            write(new ServerChatMessage(csm.getChatMessage(), getClientNumber(sender)));
        } catch (IOException e) {
            e.printStackTrace();
            // no need to kick - just a chat message
        }
    }

    // MODIFIES: this
    // EFFECTS: kicks the client of given number (1-4)
    public void kickInvalid(int playerNum) {
        kick(clients.get(IDS[playerNum - 1]), ERR_INVALID_MSG);
    }

    // MODIFIES: this
    // EFFECTS: kicks the client with given message
    public void kick(ModifiedClient c, String msg) {
        new Throwable().printStackTrace();
        try {
            c.write(NetworkMessage.packetToByteArray(new ServerKickMessage(msg)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Kicking client: " + c.ip());
        disconnect(c);
        removeClientFromEntries(c);
    }

    // MODIFIES: this
    // EFFECTS: writes out a ServerToClientMessage to clients
    public void write(ServerToClientMessage msg) throws IOException {
        write(NetworkMessage.packetToByteArray(msg));
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
        System.out.println("ReadClientToServerMessageLength: " + length);
        try {
            return (ClientToServerMessage) NetworkMessage.packetFromByteArray(msgBuffer);
        } catch (ClassNotFoundException | InvalidClassException | ClassCastException e) {
            kickInvalid(getClientNumber(c));
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
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
        try {
            clients.get(IDS[n - 1]).write(NetworkMessage.packetToByteArray(scm));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        void end() {
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
                    if (!csm.isValid()) {
                        kickInvalid(getClientNumber(c));
                    } else if (csm instanceof ClientChatMessage) {
                        handleChatMessage((ClientChatMessage) csm, c);
                    } else {
                        Main.getGameServer().addNewMessage(new MessagePair(c,csm));
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

    @Override
    public void dispose() {
        cmh.end();
        super.dispose();
    }
}
