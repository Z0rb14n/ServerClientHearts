package ui;

// TODO: DO THIS FIRST BEFORE WORKING ON CLIENT

// TODO: Use ClientToServerMessages and ServerToClient messages

import net.ClientToServerMessage;
import net.MessagePair;
import net.NewServer;
import processing.core.PApplet;
import processing.net.Client;
import processing.net.Server;
import util.Card;
import util.Deck;
import util.GameState;
import util.Suit;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static net.MessageConstants.*;

// Represents the server application
public final class ServerClientHearts extends PApplet {
    private NewServer server;
    private final static int FPS = 30;
    private GameState gameState;
    private final ArrayDeque<MessagePair> clientMessages = new ArrayDeque<>();
    private final ArrayDeque<ClientToServerMessage> clientServerMessages = new ArrayDeque<>();

    public static void main(String[] args) {
        ServerClientHearts sch = new ServerClientHearts();
        PApplet.runSketch(new String[]{"ServerClientHearts"}, sch);
    }

    //<editor-fold desc="Processing loop commands (i.e. Settings, Setup, Draw)">
    @Override
    // MODIFIES: this
    // EFFECTS: runs at beginning of program before setup - size() must be in settings() (see PApplet in processing for details)
    public void settings() {
        size(640, 480);
        gameState = new GameState();
    }

    @Override
    // MODIFIES: this
    // EFFECTS: runs at beginning of program before draw
    public void setup() {
        frameRate(FPS);
        server = new NewServer(this);
        surface.setTitle("Server Client Hearts Server?");
    }

    @Override
    // MODIFIES: this
    // EFFECTS: loops FPS times per second and renders to the screen
    public void draw() {
        background(255);
        if (!gameState.isGameStarted() && server.isFull()) {
            startGame();
        }
        if (hasNewAction()) {
            handleMessages();
        }
    }
    //</editor-fold>

    public void addNewMessage(MessagePair mp) {
        clientMessages.add(mp);
    }

    public void addNewMessage(ClientToServerMessage scm) {
        clientServerMessages.add(scm);
    }

    // EFFECTS: determines whether there is a new non-chat message to process
    public boolean hasNewMessage() {
        return !clientMessages.isEmpty();
    }

    //<editor-fold desc="Events (i.e. Client/Disconnect events)">
    // MODIFIES: this
    // EFFECTS: runs when a client connects to the server
    public void serverEvent(Server s, Client c) {
        server.onClientConnect(c);
    }

    // MODIFIES: this
    // EFFECTS: after a client has disconnected, remove them from the entries list
    public void disconnectEvent(Client c) {
        server.onClientDisconnect(c);
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
        server.onGameStart(gameState.getHandsInOrder());
    }

    // MODIFIES: this
    // EFFECTS: asks player to play 3C
    public void startFirstTurn(int starter) {
        server.startFirstTurn(starter, gameState.getHandsInOrder(), gameState.getPassingHands());
    }

    // MODIFIES: this
    // EFFECTS: asks next player to play a card
    public void requestNextCard(int justPlayed, int playerNumOfNextPlayer, Card played, Suit required) {
        server.requestNextCard(justPlayed, playerNumOfNextPlayer, gameState.getCenter(), played, required);
    }

    // MODIFIES: this
    // EFFECTS: starts new turn and writes messages to players, given "winner" (player number 1-4)
    public void startNewTurn(int winner, Deck addedPenalties) {
        server.startNewTurn(winner, addedPenalties);
    }

    // MODIFIES: this
    // EFFECTS: when game has ended - writes messages to players (who won, etc.)
    public void endGame(boolean[] winner, int points, Deck[] penaltyHands) {
        server.endGame(winner, points, penaltyHands);
    }

    public void requestKickInvalidMessage(int playerNum) {
        server.kickInvalid(playerNum);
    }

    // MODIFIES: this
    // EFFECTS: handles the messages in queue
    private void handleMessages() {
        while (!clientMessages.isEmpty()) {
            MessagePair msg = clientMessages.poll();
            // ASSUMES THERE'S ONLY PLAY MESSAGES
            System.out.print(msg.message + msg.message.matches(PLAY_MSG));
            if (!msg.message.matches(PLAY_MSG)) server.kick(msg.client, ERR_INVALID_MSG);
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
                    int clientnum = server.getClientNumber(msg.client);
                    if (clientnum != 0) {
                        gameState.playCard(clientnum, this, card1, card2, card3);
                    } // else ignore
                } catch (IllegalArgumentException | NoSuchElementException e) {
                    server.kick(msg.client, ERR_INVALID_MSG);
                }
            } else {
                // parse as one card (e.g. CARDS:3C)
                try {
                    Card card = new Card(payload);
                    int clientnum = server.getClientNumber(msg.client);
                    if (clientnum != 0) {
                        gameState.playCard(clientnum, this, card);
                    } // else ignore
                } catch (IllegalArgumentException e) {
                    server.kick(msg.client, ERR_INVALID_MSG);
                }
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: resets the server/games
    public void reset() {
        clientMessages.clear();
        server.reset();
        gameState.reset();
    }
}
