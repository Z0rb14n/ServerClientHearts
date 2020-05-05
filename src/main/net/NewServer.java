package net;

import processing.net.Client;
import processing.net.Server;
import ui.ServerClientHearts;
import util.Card;
import util.Deck;
import util.Suit;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.UUID;

import static net.MessageConstants.*;

public class NewServer extends Server {
    public final LinkedHashMap<String, Client> clients = new LinkedHashMap<>(4);
    private final static String[] ALLOWED_MESSAGES = new String[]{PLAY_MSG, CHAT_MSG};
    private final ChatMessageHandler cmh = new ChatMessageHandler();
    private ServerClientHearts sch;
    public static final int MAX_MSG_LENGTH = 0x0FFFFFFF;
    public static final int PORT = 5204;
    public final String[] IDS = new String[4];

    public NewServer(ServerClientHearts parent) {
        super(parent, PORT);
        sch = parent;
        System.out.println("Server started at: " + Server.ip());
        cmh.start();
    }

    // MODIFIES: this
    // EFFECTS: resets the server (i.e. clears all remaining messages and writes out a reset message)
    public void reset() {
        write(RESET);
    }

    // EFFECTS: determines whether there is not enough space for other players
    public boolean isFull() {
        return firstEmptySpace() == -1;
    }

    // MODIFIES: this
    // EFFECTS: runs when the game has started
    public void onGameStart(Deck[] decks) {
        if (decks.length != 4) throw new IllegalArgumentException();
        write(START_GAME_MSG);
        clients.get(IDS[0]).write(STARTING_HAND + decks[0].toString());
        clients.get(IDS[1]).write(STARTING_HAND + decks[1].toString());
        clients.get(IDS[2]).write(STARTING_HAND + decks[2].toString());
        clients.get(IDS[3]).write(STARTING_HAND + decks[3].toString());
    }

    public void startFirstTurn(int starter, Deck[] hands) {
        if (starter < 1 || starter > 4) throw new IllegalArgumentException("Invalid argument: " + starter);
        if (hands.length != 4) throw new IllegalArgumentException();
        sendNthClientMessage(1, NEW_HAND + hands[0].toString());
        sendNthClientMessage(2, NEW_HAND + hands[1].toString());
        sendNthClientMessage(3, NEW_HAND + hands[2].toString());
        sendNthClientMessage(4, NEW_HAND + hands[3].toString());
        write(START_ROUND);
        sendNthClientMessage(starter, START_3C);
    }

    public void requestNextCard(int justPlayed, int nextPlayerNum, Deck center, Card played, Suit required) {
        write(PREVIOUS_CARD_HEADER + justPlayed + "," + played.toString());
        write(CENTER_HAND + center.toString());
        getNthClient(nextPlayerNum - 1).write(REQUEST_CARD_HEADER + required.toString());
    }

    public void startNewTurn(int winner, Deck addedPenalties) {
        write(END_ROUND);
        write(ROUND_WINNER + winner + addedPenalties.toString());
        write(START_ROUND);
    }

    public void endGame(int winner, int points) {
        write(END_ROUND);
        write(END_GAME);
        write(GAME_WINNER_HEADER + winner + ",POINTS:" + points);
    }

    // MODIFIES: this
    // EFFECTS: runs when a new client connects
    public void onClientConnect(Client c) {
        System.out.println("New client " + c.ip() + " connected to server  " + Server.ip());
        int spot = firstEmptySpace();
        if (spot == -1) {
            c.write(ERR_TOO_MANY_PLAYERS);
            disconnect(c);
        } else {
            String id = UUID.randomUUID().toString();
            c.write("P" + (spot + 1) + "ID:" + id);
            clients.put(id, c);
            IDS[spot] = id;
            informNewPlayerGameDetails(spot + 1);
            informPlayersPlayerJoined(spot + 1);
            System.out.println(clientCount);
        }
    }

    // MODIFIES: this
    // EFFECTS: runs when a client disconnects
    public void onClientDisconnect(Client c) {
        System.out.println("Client disconnected: " + c.ip());
        informPlayersOnDisconnect(c);
        removeClientFromEntries(c);
    }

    // EFFECTS: messages all other clients that a player has joined
    private void informPlayersPlayerJoined(int playerNumber) {
        for (int i = 0; i < IDS.length; i++) {
            if (IDS[i] != null && clients.get(IDS[i]) != null && i != playerNumber - 1) {
                clients.get(IDS[i]).write(NEW_PLAYER_HEADER + playerNumber);
            }
        }
    }

    // EFFECTS: messages new player about who's around
    private void informNewPlayerGameDetails(int playerNumber) {
        StringBuilder sb = new StringBuilder(CURRENT_PLAYERS_HEADER);
        for (int i = 0; i < IDS.length; i++) {
            if (IDS[i] != null && i != playerNumber - 1) {
                sb.append((i + 1));
            }
        }
        if (sb.toString().equals(CURRENT_PLAYERS_HEADER)) sb.append("NONE");
        clients.get(IDS[playerNumber - 1]).write(sb.toString());
    }

    // MODIFIES: this
    // EFFECTS: informs other players that a player has disconnected
    private void informPlayersOnDisconnect(Client c) {
        int playerNum = getClientNumber(c);
        if (playerNum == 0) return;
        write(DISCONNECT_PLAYER_HEADER + playerNum);
    }

    // EFFECTS: returns client number (1-4), 0 if non-existent
    public int getClientNumber(Client c) {
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
    // EFFECTS: checks that a message is valid. If not, kickInvalid the client.
    private boolean checkValidMessage(String msg, Client c) {
        for (String msgType : ALLOWED_MESSAGES) {
            if (msg.matches(msgType)) return true;
        }
        kick(c, ERR_INVALID_MSG);
        return false;
    }

    // MODIFIES: this
    // EFFECTS: handles all chat messages.
    private void handleChatMessage(String msg, Client sender) {
        int clientNum = getClientNumber(sender);
        final String header = "CHAT" + clientNum + ":";
        for (int i = 0; i < 4; i++) {
            if (IDS[i] != null) clients.get(IDS[i]).write(header + msg.substring(CHAT_MSG_INDEX));
        }
    }

    // MODIFIES: this
    // EFFECTS: kicks the client of given number (1-4)
    public void kickInvalid(int playerNum) {
        kick(clients.get(IDS[playerNum - 1]), ERR_INVALID_MSG);
    }

    // MODIFIES: this
    // EFFECTS: kicks the client
    public void kick(Client c) {
        kick(c, KICK_DEFAULT_MSG);
    }

    public void write(ServerToClientMessage msg) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(msg);
            oos.flush();
            byte[] bytes = bos.toByteArray();
            if (bytes.length > MAX_MSG_LENGTH) throw new RuntimeException("AAAAAAAAAAAAAAAAAAA");
            write(bytes.length); //TODO -- WOULD NOT WORK - MAX LENGTH IS 255
            write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // EFFECTS: reads a ClientToServer message from a client
    //    NOTE: THIS WILL COMPLETELY FREEZE EXECUTION UNTIL THIS THING RECEIVES THE WHOLE MESSAGE
    public ClientToServerMessage readClientToServerMessage(Client c) {
        if (c.available() <= 0) throw new IllegalArgumentException("Client has not written to server.");
        int length = c.read(); //TODO -- WOULD NOT WORK - MAX LENGTH IS 255
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
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ClientToServerMessage byteToClientToServerMessage(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ObjectInputStream in = new ObjectInputStream(bis)) {
            return (ClientToServerMessage) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // MODIFIES: this
    // EFFECTS: kicks the client with given message
    public void kick(Client c, String msg) {
        c.write(msg);
        disconnect(c);
        System.out.println("Client kicked: " + c.ip());
        removeClientFromEntries(c);
    }

    // MODIFIES: this
    // EFFECTS: removes a client from the list of entries
    private void removeClientFromEntries(Client c) {
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

    // EFFECTS: gets Client with given client number (1-4)
    private Client getNthClient(int n) {
        if (n < 1 || n > 4) throw new IllegalArgumentException("n must be a number from 1-4");
        if (IDS[n - 1] == null) return null;
        if (!clients.containsKey(IDS[n - 1])) return null;
        return clients.get(IDS[n - 1]);
    }

    // EFFECTS: sends Nth client a message
    private void sendNthClientMessage(int n, String msg) {
        getNthClient(n).write(msg);
    }

    // EFFECTS: returns the first empty index (0-3) for IDs
    private int firstEmptySpace() {
        if (IDS[0] == null) return 0;
        if (IDS[1] == null) return 1;
        if (IDS[2] == null) return 2;
        if (IDS[3] == null) return 3;
        return -1;
    }

    // EFFECTS: returns whether a message is a chat message
    public static boolean isChatMessage(String msg) {
        return msg.matches(CHAT_MSG);
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
                Client c = available();
                while (c != null) {
                    String sent = c.readString();
                    System.out.println("Client number " + getClientNumber(c) + " has sent " + sent);
                    boolean result = checkValidMessage(sent, c);
                    if (result) {
                        if (isChatMessage(sent)) handleChatMessage(sent, c);
                        else sch.addNewMessage(new MessagePair(c, sent));
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
