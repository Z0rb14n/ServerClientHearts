package ui;

// TODO: DO THIS FIRST BEFORE WORKING ON CLIENT

import processing.core.PApplet;
import processing.net.Client;
import processing.net.Server;
import util.Deck;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.UUID;

public class ServerClientHearts extends PApplet {
    Server server;
    LinkedHashMap<String, Client> clients;
    public final static String ERR_TOO_MANY_PLAYERS = "ERR: TOO MANY PLAYERS";
    public final static String ERR_INVALID_MSG = "ERR: INVALID MSG";
    public final static String KICK_DEFAULT_MSG = "ERR: KICKED";
    public final static String START_GAME_MSG = "START GAME";
    public final static String CHAT_MSG_HEADER = "CHAT:";
    public final static String CHAT_MSG = CHAT_MSG_HEADER + ".+";
    public final static int CHAT_MSG_INDEX = CHAT_MSG_HEADER.length();
    public final static String OUTGOING_CHAT_MSG = "CHAT\\d:.+";
    public final static String PLAY_MSG_HEADER = "CARDS:";
    public final static String PLAY_MSG = PLAY_MSG_HEADER+".+";
    public final static int PLAY_MSG_INDEX = PLAY_MSG_HEADER.length();
    public final static String PLAYER_ID_HEADER = "P\\dID:.+";
    public final static String STARTING_HAND = "START:";
    String[] IDS = new String[4];
    public static final int port = 5204;
    ArrayDeque<String> clientMessages;
    
    boolean gameStarted = false;
    
    Deck player1Hand;
    Deck player2Hand;
    Deck player3Hand;
    Deck player4Hand;

    @Override
    public void settings() {
        size(640, 480);
        player1Hand = new Deck();
        player2Hand = new Deck();
        player3Hand = new Deck();
        player4Hand = new Deck();
        clientMessages = new ArrayDeque<>();
    }

    @Override
    public void setup() {
        frameRate(30);
        clients = new LinkedHashMap<>(4);
        server = new Server(this, port);
        System.out.println("Server started at: " + Server.ip());
        surface.setTitle("Server Client Hearts Server?");
    }

    @Override
    public void draw() {
        background(255);
        if (!gameStarted && firstEmptySpace() == -1) {
            startGame();
        }
        addNewMessages();
        if (hasNewAction()) {
            // do action
        }
    }
    
    public boolean hasNewAction() {
        return !clientMessages.isEmpty();
    }
    
    public void startGame() {
        gameStarted = true;
            server.write(START_GAME_MSG);
            // start the game
            Deck temp = new Deck();
            temp.generate52();
            temp.randomlyDistribute(player1Hand, player2Hand, player3Hand, player4Hand);
            getNthClient(0).write(STARTING_HAND + player1Hand.toString());
            getNthClient(1).write(STARTING_HAND + player2Hand.toString());
            getNthClient(2).write(STARTING_HAND + player3Hand.toString());
            getNthClient(3).write(STARTING_HAND + player4Hand.toString());
    }
    
    public void addNewMessages() {
        Client c = server.available();
        while (c != null) {
            String sent = c.readString();
            System.out.println("Client number " + getClientNumber(c) + " has sent " + sent);
            checkValidMessage(sent,c);
            if (isChatMessage(sent)) handleChatMessage(sent,c);
            else clientMessages.add(sent);
            c = server.available(); // get next client 
        }
    }
    
    public boolean isChatMessage(String msg) {
        return msg.matches(CHAT_MSG);
    }
    
    public void checkValidMessage(String msg, Client c) {
        if (!isChatMessage(msg) && !msg.matches(PLAY_MSG)) kick(c);
    }
    
    public void handleChatMessage(String msg, Client sender) {
        int clientNum = getClientNumber(sender);
        final String header = "CHAT" + clientNum + ":";
        for(int i = 0; i < 4; i++) {
            if ((i+1) == clientNum) continue;
            clients.get(IDS[i]).write(header + msg.substring(CHAT_MSG_INDEX));
        }
    }
    
    public void handleMessages() {
        while (!clientMessages.isEmpty()) {
            String msg = clientMessages.poll();
            // do something
        }
    }

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
    
    public void kick(Client c) {
        kick(c,KICK_DEFAULT_MSG);
    }
    
    public void kick(Client c, String msg) {
        c.write(msg);
        server.disconnect(c);
        System.out.println("Client kicked: " + c.ip());
        removeClientFromEntries(c);
    }
    
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
    
    /**
     * 
     * @param n number from 1-4
     * @return 
     */
    public Client getNthClient(int n) {
        if (n < 1 || n > 4) throw new IllegalArgumentException("n must be a number from 1-4");
        if (IDS[n-1] == null) return null;
        if (!clients.containsKey(IDS[n-1])) return null;
        return clients.get(IDS[n-1]);
    }
    /**
     * 
     * @param c client
     * @return number 1-4, 0 if unavailable
     */
    public int getClientNumber(Client c) {
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
    /**
     * 
     * @return index from 0-3
     */
    public int firstEmptySpace() {
        if (IDS[0] == null) return 0;
        if (IDS[1] == null) return 1;
        if (IDS[2] == null) return 2;
        if (IDS[3] == null) return 3;
        return -1;
    }
    
    public void disconnectEvent(Client c) {
        System.out.println("Client disconnected: " + c.ip());
        removeClientFromEntries(c);
    }

    public static void main(String[] args) {
        String[] processingArgs = {"lmao"};
        ServerClientHearts sch = new ServerClientHearts();
        PApplet.runSketch(processingArgs, sch);
    }

}
