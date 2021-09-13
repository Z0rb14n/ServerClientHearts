package ui.server;

import net.*;
import net.message.client.ClientCardMessage;
import net.message.client.ClientThreeCardMessage;
import util.GameState;
import util.card.Card;
import util.card.Deck;
import util.card.Suit;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;

import static net.Constants.ERR_INVALID_MSG;

public class ServerFrame extends JFrame implements EventReceiver {
    private static ServerFrame ourInstance;
    private static final Dimension SIZE = new Dimension(640, 480);
    private final Timer updateTimer = new Timer(100, e -> repaint());
    private final ArrayDeque<MessagePair> clientMessages = new ArrayDeque<>();
    private final ModifiedNewServer newServer = new ModifiedNewServer(this);
    private final GameState gameState = new GameState();

    public static ServerFrame getInstance() {
        if (ourInstance == null) {
            ourInstance = new ServerFrame();
        }
        return ourInstance;
    }

    private ServerFrame() {
        super("Server Client Hearts Server");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(SIZE);
        setBackground(Color.WHITE);
        setSize(SIZE);
        updateTimer.start();
        setVisible(true);
    }

    @Override
    // MODIFIES: g, this
    // EFFECTS: paints the given graphics object and updates the frame
    public void paint(Graphics g) {
        super.paint(g);
        update();
    }

    @Override
    // MODIFIES: this
    // EFFECTS: disposes the jframe
    public void dispose() {
        updateTimer.stop();
        newServer.stop();
        super.dispose();
    }

    // MODIFIES: this
    // EFFECTS: updates the game state
    private void update() {
        if (!gameState.isGameStarted() && newServer.isFull()) {
            startGame();
        }
        if (hasNewAction()) {
            handleMessages();
        }
    }

    // MODIFIES: this
    // EFFECTS: adds the message to the server and processes it
    public void addNewMessage(MessagePair mp) {
        clientMessages.add(mp);
    }

    // EFFECTS: determines whether there is a new non-chat message to process
    public boolean hasNewMessage() {
        return !clientMessages.isEmpty();
    }

    //<editor-fold desc="Events (i.e. Client/Disconnect events)">
    @Override
    // MODIFIES: this
    // EFFECTS: runs when a client connects to the server
    public void clientConnectionEvent(ModifiedServer s, ModifiedClient c) {
        newServer.onClientConnect(c);
    }

    @Override
    // MODIFIES: this
    // EFFECTS: after a client has disconnected, remove them from the entries list
    public void disconnectEvent(ModifiedClient c) {
        newServer.onClientDisconnect(c);
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
        newServer.onGameStart(gameState.getHandsInOrder());
    }


    // MODIFIES: this
    // EFFECTS: asks player to play 3C
    public void startFirstTurn(int starter) {
        newServer.startFirstTurn(starter, gameState.getHandsInOrder(), gameState.getPassingHands());
    }

    // MODIFIES: this
    // EFFECTS: asks next player to play a card
    public void requestNextCard(int justPlayed, int playerNumOfNextPlayer, Card played, Suit required) {
        newServer.requestNextCard(justPlayed, playerNumOfNextPlayer, gameState.getCenter(), played, required);
    }

    // MODIFIES: this
    // EFFECTS: starts new turn and writes messages to players, given "winner" (player number 1-4)
    public void startNewTurn(int winner, Deck addedPenalties) {
        newServer.startNewTurn(winner, addedPenalties);
    }

    // MODIFIES: this
    // EFFECTS: when game has ended - writes messages to players (who won, etc.)
    public void endGame(boolean[] winner, int points, Deck[] penaltyHands) {
        newServer.endGame(winner, points, penaltyHands);
    }

    // MODIFIES: this
    // EFFECTS: kicks a player due to an invalid message
    public void requestKickInvalidMessage(int playerNum) {
        newServer.kickInvalid(playerNum);
    }

    // MODIFIES: this
    // EFFECTS: handles the messages in queue
    private void handleMessages() {
        while (!clientMessages.isEmpty()) {
            MessagePair msg = clientMessages.poll();
            // ASSUMES THERE'S ONLY PLAY MESSAGES

            if (!msg.msg.isValid() && !(msg.msg instanceof ClientCardMessage) && !(msg.msg instanceof ClientThreeCardMessage)) {
                System.err.println("Received non-play message for play message handler");
                newServer.kick(msg.modifiedClient, ERR_INVALID_MSG); // this only accepts play messages
            }
            int clientNum = newServer.getClientNumber(msg.modifiedClient);
            if (msg.msg instanceof ClientThreeCardMessage) {
                if (clientNum != 0) {
                    Deck deck = ((ClientThreeCardMessage) msg.msg).getCards();
                    gameState.playCard(clientNum, deck.get(0), deck.get(1), deck.get(2));
                }
            } else if (msg.msg instanceof ClientCardMessage) {
                try {
                    if (clientNum != 0) {
                        Card card = ((ClientCardMessage) msg.msg).getCard();
                        gameState.playCard(clientNum, card);
                    }
                } catch (IllegalArgumentException e) {
                    newServer.kick(msg.modifiedClient, ERR_INVALID_MSG);
                }
            }

        }
    }

    // MODIFIES: this
    // EFFECTS: resets the server/games
    public void reset() {
        clientMessages.clear();
        newServer.reset();
        gameState.reset();
    }
}
