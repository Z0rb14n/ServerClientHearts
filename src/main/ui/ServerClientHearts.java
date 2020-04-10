package ui;

// TODO: DO THIS FIRST BEFORE WORKING ON CLIENT

// TODO: CARD MESSAGE PARSER -> SEND PARSED MESSAGE INTO GAME STATE UNDER PLAYCARD(PLAYERNUM, CALLER, CARDS)

import processing.core.PApplet;
import processing.net.Client;
import processing.net.Server;
import util.Card;
import util.GameState;
import util.Suit;

import java.util.*;

// Represents the server application
public class ServerClientHearts extends PApplet {
    private Server server;
    private LinkedHashMap<String, Client> clients;
    private final static String ERR_TOO_MANY_PLAYERS = "ERR: TOO MANY PLAYERS";
    private final static String ERR_INVALID_MSG = "ERR: INVALID MSG";
    private final static String KICK_DEFAULT_MSG = "ERR: KICKED";
    private final static String START_GAME_MSG = "START GAME";
    private final static String CHAT_MSG_HEADER = "CHAT:";
    private final static String CHAT_MSG = CHAT_MSG_HEADER + ".+";
    private final static int CHAT_MSG_INDEX = CHAT_MSG_HEADER.length();
    private final static String OUTGOING_CHAT_MSG = "CHAT\\d:.+";
    private final static String PLAY_MSG_HEADER = "CARDS:";
    private final static String PLAY_MSG = PLAY_MSG_HEADER + ".+";
    private final static int PLAY_MSG_INDEX = PLAY_MSG_HEADER.length();
    private final static String PLAYER_ID_HEADER = "P\\dID:.+";
    private final static String STARTING_HAND = "START:";
    private final static String RESET = "RESET";
    private final static String CARD_DELIMITER = ",";
    private final static String[] ALLOWED_MESSAGES = new String[]{PLAY_MSG, CHAT_MSG};
    private final static int FPS = 30;
    private final String[] IDS = new String[4];
    private static final int port = 5204;
    private ArrayDeque<MessagePair> clientMessages;

    private GameState gameState;
    private MessageHandler cmh;

    public static void main(String[] args) {
        ServerClientHearts sch = new ServerClientHearts();
        PApplet.runSketch(new String[]{"ServerClientHearts"}, sch);
    }

    // EFFECTS: returns whether a message is a chat message
    public static boolean isChatMessage(String msg) {
        return msg.matches(CHAT_MSG);
    }

    @Override
    // MODIFIES: this
    // EFFECTS: runs at beginning of program before setup - size() must be in settings() (see PApplet in processing for details)
    public void settings() {
        size(640, 480);
        gameState = new GameState();
        clientMessages = new ArrayDeque<>();
        clients = new LinkedHashMap<>(4);
        cmh = new MessageHandler();
    }

    @Override
    // MODIFIES: this
    // EFFECTS: runs at beginning of program before draw
    public void setup() {
        frameRate(FPS);
        server = new Server(this, port);
        System.out.println("Server started at: " + Server.ip());
        surface.setTitle("Server Client Hearts Server?");
        cmh.start();
    }

    // EFFECTS: returns whether there is another client message to process
    private boolean hasNewAction() {
        return !clientMessages.isEmpty();
    }

    // MODIFIES: this
    // EFFECTS: starts the current game of hearts and transitions to "pass cards" stage
    private void startGame() {
        gameState.startGame();
        server.write(START_GAME_MSG);
        try {
            getNthClient(1).write(STARTING_HAND + gameState.getPlayerOneHand().toString());
            getNthClient(2).write(STARTING_HAND + gameState.getPlayerTwoHand().toString());
            getNthClient(3).write(STARTING_HAND + gameState.getPlayerThreeHand().toString());
            getNthClient(4).write(STARTING_HAND + gameState.getPlayerFourHand().toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    // MODIFIES: this
    // EFFECTS: loops FPS times per second and renders to the screen
    public void draw() {
        background(255);
        if (!gameState.isGameStarted() && firstEmptySpace() == -1) {
            startGame();
        }
        if (hasNewAction()) {
            handleMessages();
        }
    }

    // MODIFIES: this
    // EFFECTS: handles all chat messages.
    private void handleChatMessage(String msg, Client sender) {
        int clientNum = getClientNumber(sender);
        final String header = "CHAT" + clientNum + ":";
        for(int i = 0; i < 4; i++) {
            if (IDS[i] != null) clients.get(IDS[i]).write(header + msg.substring(CHAT_MSG_INDEX));
        }
    }

    // MODIFIES: this
    // EFFECTS: checks that a message is valid. If not, kickInvalid the client.
    private boolean checkValidMessage(String msg, Client c) {
        for (String msgType : ALLOWED_MESSAGES) {
            if (msg.matches(msgType)) return true;
        }
        kick(c);
        return false;
    }

    // TODO METHOD BODY
    // MODIFIES: this
    // EFFECTS: asks player to play 3C
    public void startFirstTurn(int starter) {
        // TODO METHOD BODY
    }

    // TODO METHOD BODY
    // MODIFIES: this
    // EFFECTS: asks next player to play a card
    public void requestNextCard(int justPlayed, int playerNumOfNextPlayer, Card played, Suit required) {
        // TODO METHOD BODY
    }

    // TODO METHOD BODY
    // MODIFIES: this
    // EFFECTS: starts new turn and writes messages to players, given "winner" (player number 1-4)
    public void startNewTurn(int winner) {
        // TODO METHOD BODY
    }

    // TODO METHOD BODY
    // MODIFIES: this
    // EFFECTS: when game has ended - writes messages to players (who won, etc.)
    public void endGame() {
        // TODO METHOD BODY
    }

    // MODIFIES: this
    // EFFECTS: handles the messages in queue
    private void handleMessages() {
        while (!clientMessages.isEmpty()) {
            MessagePair msg = clientMessages.poll();
            // ASSUMES THERE'S ONLY PLAY MESSAGES
            if (!msg.message.matches(PLAY_MSG)) kick(msg.client, ERR_INVALID_MSG);
            String payload = msg.message.substring(PLAY_MSG_INDEX);
            payload = payload.trim();
            if (payload.contains(",")) {
                // parse as 3 cards (e.g. CARDS:3C,4C,5C)
                try (Scanner scanner = new Scanner(payload)) {
                    Card card1 = new Card(scanner.next());
                    Card card2 = new Card(scanner.next());
                    Card card3 = new Card(scanner.next());
                    int clientnum = getClientNumber(msg.client);
                    if (clientnum != 0) {
                        gameState.playCard(clientnum, this, card1, card2, card3);
                    } // else ignore
                } catch (IllegalArgumentException | NoSuchElementException e) {
                    kick(msg.client, ERR_INVALID_MSG);
                }
            } else {
                // parse as one card (e.g. CARDS:3C)
                try {
                    Card card = new Card(payload);
                    int clientnum = getClientNumber(msg.client);
                    if (clientnum != 0) {
                        gameState.playCard(clientnum, this, card);
                    } // else ignore
                } catch (IllegalArgumentException e) {
                    kick(msg.client, ERR_INVALID_MSG);
                }
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: resets the server/games
    public void reset() {
        clientMessages.clear();
        for (Client c : clients.values()) {
            c.write(RESET);
        }
        gameState.reset();
    }

    // MODIFIES: this
    // EFFECTS: runs when a client connects to the server
    public void serverEvent(Server s, Client c) {
        System.out.println("New client " + c.ip() + " connected to server  " + Server.ip());
        int spot = firstEmptySpace();
        if (spot == -1) {
            c.write(ERR_TOO_MANY_PLAYERS);
            s.disconnect(c);
        } else {
            String id = UUID.randomUUID().toString();
            c.write("P" + (spot+1) + "ID:" + id);
            clients.put(id, c);
            IDS[spot] = id;
            System.out.println(s.clientCount);
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
        kick(c,KICK_DEFAULT_MSG);
    }

    // MODIFIES: this
    // EFFECTS: kicks the client with given message
    public void kick(Client c, String msg) {
        c.write(msg);
        server.disconnect(c);
        System.out.println("Client kicked: " + c.ip());
        removeClientFromEntries(c);
    }

    // MODIFIES: this
    // EFFECTS: removes a client from the list of entries
    private void removeClientFromEntries(Client c) {
        if (!clients.containsValue(c)) return;
        String toRemove = null;
        for(String s : clients.keySet()) {
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
        assert(toRemove != null);
        clients.remove(toRemove);
        System.out.println("Successfully removed id " + toRemove + ", ip: " + c.ip());
    }

    // EFFECTS: gets Client with given client number (1-4)
    private Client getNthClient(int n) {
        if (n < 1 || n > 4) throw new IllegalArgumentException("n must be a number from 1-4");
        if (IDS[n-1] == null) return null;
        if (!clients.containsKey(IDS[n-1])) return null;
        return clients.get(IDS[n-1]);
    }

    // EFFECTS: returns client number (1-4), 0 if non-existent
    private int getClientNumber(Client c) {
        if (!clients.containsValue(c)) return 0;
        for (String id : clients.keySet()) {
            if (clients.get(id).equals(c)) {
                if (IDS[0].equals(id)) return 1;
                if (IDS[1].equals(id)) return 2;
                if (IDS[2].equals(id)) return 3;
                if (IDS[3].equals(id)) return 4;
                else return 0;
            }
        }
        return 0;
    }

    // EFFECTS: returns the first empty index (0-3) for IDs
    private int firstEmptySpace() {
        if (IDS[0] == null) return 0;
        if (IDS[1] == null) return 1;
        if (IDS[2] == null) return 2;
        if (IDS[3] == null) return 3;
        return -1;
    }

    // MODIFIES: this
    // EFFECTS: after a client has disconnected, remove them from the entries list
    public void disconnectEvent(Client c) {
        System.out.println("Client disconnected: " + c.ip());
        removeClientFromEntries(c);
    }

    // Represents the thread that handles the chat messages
    private class MessageHandler extends Thread {
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
                Client c = server.available();
                while (c != null) {
                    String sent = c.readString();
                    System.out.println("Client number " + getClientNumber(c) + " has sent " + sent);
                    boolean result = checkValidMessage(sent, c);
                    if (result) {
                        if (isChatMessage(sent)) handleChatMessage(sent, c);
                        else clientMessages.add(new MessagePair(c, sent));
                    }
                    c = server.available(); // get next client
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
