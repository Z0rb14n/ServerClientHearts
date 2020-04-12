package ui;

// TODO: DO THIS FIRST BEFORE WORKING ON CLIENT

import net.MessagePair;
import processing.core.PApplet;
import processing.net.Client;
import processing.net.Server;
import util.Card;
import util.Deck;
import util.GameState;
import util.Suit;

import java.util.*;

// Represents the server application
public class ServerClientHearts extends PApplet {
    private Server server;
    private LinkedHashMap<String, Client> clients;
    public final static int PORT = 5204;
    private final static int FPS = 30;
    private ArrayDeque<MessagePair> clientMessages;
    private GameState gameState;
    private MessageHandler cmh;

    //<editor-fold desc="MESSAGE HEADERS">
    public final static String ERROR = "ERR: ";
    private final static String NEW_PLAYER_HEADER = "NEW PLAYER:";
    private final static String DISCONNECT_PLAYER_HEADER = "DISCONNECT:";
    private final static String CURRENT_PLAYERS_HEADER = "CURRENT PLAYERS:";
    private final static String REQUEST_CARD_HEADER = "PLAY:";
    private final static String PREVIOUS_CARD_HEADER = "PLAYED:";
    private final static String PLAY_MSG_HEADER = "CARDS:";
    private final static String ROUND_WINNER_HEADER = "WINNER:";
    private final static String GAME_WINNER_HEADER = "GAME WINNER:";
    //</editor-fold>
    //<editor-fold desc="MESSAGE FORMATS">
    public final static String ERROR_FORMAT = "ERR: .+";
    private final static String CARD_DELIMITER = ",";
    private final static String REQUEST_CARD_MSG = REQUEST_CARD_HEADER + ".+";
    private final static String PREVIOUS_CARD_MSG = PREVIOUS_CARD_HEADER + "\\d,.+";
    private final static String ROUND_WINNER = ROUND_WINNER_HEADER + "\\d.*";
    private final static String GAME_WINNER = GAME_WINNER_HEADER + "\\d" + ",POINTS:\\d+";
    public final static String ERR_TOO_MANY_PLAYERS = ERROR + "TOO MANY PLAYERS";
    public final static String ERR_INVALID_MSG = ERROR + "INVALID MSG";
    public final static String NEW_PLAYER_MSG = NEW_PLAYER_HEADER + "\\d";
    public final static String DISCONNECT_PLAYER_MSG = DISCONNECT_PLAYER_HEADER + "\\d";
    private final static String KICK_DEFAULT_MSG = ERROR + "KICKED";
    private final static String RESET = "RESET";
    private final static String CURRENT_PLAYERS_MSG = CURRENT_PLAYERS_HEADER + "\\d*";
    private final static String START_GAME_MSG = "START GAME";
    private final static String CHAT_MSG_HEADER = "CHAT:";
    private final static String CHAT_MSG = CHAT_MSG_HEADER + ".+";
    private final static int CHAT_MSG_INDEX = CHAT_MSG_HEADER.length();
    private final static String OUTGOING_CHAT_MSG = "CHAT\\d:.+";
    private final static String PLAY_MSG = PLAY_MSG_HEADER + ".+";
    private final static int PLAY_MSG_INDEX = PLAY_MSG_HEADER.length();
    private final static String PLAYER_ID_HEADER = "P\\dID:.+";
    private final static String CENTER_HAND = "CENTER:";
    private final static String STARTING_HAND = "STARTING_HAND:";
    private final static String NEW_HAND = "NEW_HAND:";
    private final static String START_ROUND = "START_ROUND";
    private final static String END_ROUND = "END ROUND";
    private final static String END_GAME = "END GAME";
    private final static String START_3C = "START_3C";
    //</editor-fold>
    private final String[] IDS = new String[4];
    private final static String[] ALLOWED_MESSAGES = new String[]{PLAY_MSG, CHAT_MSG};

    public static void main(String[] args) {
        ServerClientHearts sch = new ServerClientHearts();
        PApplet.runSketch(new String[]{"ServerClientHearts"}, sch);
    }

    // EFFECTS: returns whether a message is a chat message
    public static boolean isChatMessage(String msg) {
        return msg.matches(CHAT_MSG);
    }

    //<editor-fold desc="Processing loop commands (i.e. Settings, Setup, Draw)">
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
        server = new Server(this, PORT);
        System.out.println("Server started at: " + Server.ip());
        surface.setTitle("Server Client Hearts Server?");
        cmh.start();
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
    //</editor-fold>

    //<editor-fold desc="On-mouse/on-key Processing commands">
    //</editor-fold>
    //<editor-fold desc="Events (i.e. Client/Disconnect events)">
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
            c.write("P" + (spot + 1) + "ID:" + id);
            clients.put(id, c);
            IDS[spot] = id;
            informNewPlayerGameDetails(spot + 1);
            informPlayersPlayerJoined(spot + 1);
            System.out.println(s.clientCount);
        }
    }

    // MODIFIES: this
    // EFFECTS: after a client has disconnected, remove them from the entries list
    public void disconnectEvent(Client c) {
        System.out.println("Client disconnected: " + c.ip());
        informPlayersOnDisconnect(c);
        removeClientFromEntries(c);
    }
    //</editor-fold>

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
        kick(c, ERR_INVALID_MSG);
        return false;
    }

    // MODIFIES: this
    // EFFECTS: asks player to play 3C
    public void startFirstTurn(int starter) {
        sendNthClientMessage(1, NEW_HAND + gameState.getPlayerOneHand().toString());
        sendNthClientMessage(2, NEW_HAND + gameState.getPlayerTwoHand().toString());
        sendNthClientMessage(3, NEW_HAND + gameState.getPlayerThreeHand().toString());
        sendNthClientMessage(4, NEW_HAND + gameState.getPlayerFourHand().toString());
        server.write(START_ROUND);
        sendNthClientMessage(starter, START_3C);
    }

    // MODIFIES: this
    // EFFECTS: asks next player to play a card
    public void requestNextCard(int justPlayed, int playerNumOfNextPlayer, Card played, Suit required) {
        server.write(PREVIOUS_CARD_HEADER + justPlayed + "," + played.toString());
        server.write(CENTER_HAND + gameState.getCenter().toString());
        getNthClient(playerNumOfNextPlayer - 1).write(REQUEST_CARD_HEADER + required.toString());
    }

    // MODIFIES: this
    // EFFECTS: starts new turn and writes messages to players, given "winner" (player number 1-4)
    public void startNewTurn(int winner, Deck addedPenalties) {
        server.write(END_ROUND);
        server.write(ROUND_WINNER + winner + addedPenalties.toString());
        server.write(START_ROUND);
    }

    // MODIFIES: this
    // EFFECTS: when game has ended - writes messages to players (who won, etc.)
    public void endGame(int winner, int points) {
        server.write(END_ROUND);
        server.write(END_GAME);
        server.write(GAME_WINNER_HEADER + winner + ",POINTS:" + points);
    }

    // MODIFIES: this
    // EFFECTS: handles the messages in queue
    private void handleMessages() {
        while (!clientMessages.isEmpty()) {
            MessagePair msg = clientMessages.poll();
            // ASSUMES THERE'S ONLY PLAY MESSAGES
            System.out.print(msg.message + msg.message.matches(PLAY_MSG));
            if (!msg.message.matches(PLAY_MSG)) kick(msg.client, ERR_INVALID_MSG);
            String payload = msg.message.substring(PLAY_MSG_INDEX);
            payload = payload.trim();
            System.out.print(payload + payload.contains(","));
            if (payload.contains(",")) {
                // parse as 3 cards (e.g. CARDS:3C,4C,5C)
                try (Scanner scanner = new Scanner(payload).useDelimiter(CARD_DELIMITER)) {
                    String l1, l2, l3;
                    l1 = scanner.next();
                    l2 = scanner.next();
                    l3 = scanner.next();
                    System.out.print(l1 + l2 + l3);
                    Card card1 = new Card(l1);
                    Card card2 = new Card(l2);
                    Card card3 = new Card(l3);
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

    // EFFECTS: messages all other clients that a player has joined
    private void informPlayersPlayerJoined(int playerNumber) {
        for (int i = 0; i < IDS.length; i++) {
            if (IDS[i] != null && i != playerNumber - 1) {
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
        server.write(DISCONNECT_PLAYER_HEADER + playerNum);
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

    // EFFECTS: sends Nth client a message
    private void sendNthClientMessage(int n, String msg) {
        getNthClient(n).write(msg);
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
