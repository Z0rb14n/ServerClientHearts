package server;

import net.*;
import net.message.client.ClientCardMessage;
import net.message.client.ClientThreeCardMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import util.card.Card;
import util.card.Deck;
import util.card.Suit;

import java.util.ArrayDeque;

import static net.Constants.ERR_INVALID_MSG;

public class GameServer implements EventReceiver {
    private final ArrayDeque<MessagePair> clientMessages = new ArrayDeque<>();
    private final ModifiedNewServer newServer = new ModifiedNewServer(this);
    private final ServerGameState gameState = new ServerGameState();

    /**
     * Disposes/frees the resources of this GameServer
     */
    @Contract(mutates = "this")
    public void dispose() {
        newServer.stop();
        clientMessages.clear();
    }

    /**
     * Updates the game state
     */
    @Contract(mutates = "this")
    public void update() {
        if (!gameState.isGameStarted() && newServer.isFull()) {
            startGame();
        }
        if (hasNewAction()) {
            handleMessages();
        }
    }

    /**
     * Adds a message pair to a queue/backlog of messages to process
     *
     * @param mp message pair to add
     */
    @Contract(mutates = "this")
    public void addNewMessage(MessagePair mp) {
        clientMessages.add(mp);
    }

    /**
     * @return true if there is a new non-chat message to process
     */
    @Contract(pure = true)
    public boolean hasNewMessage() {
        return !clientMessages.isEmpty();
    }

    //<editor-fold desc="Events (i.e. Client/Disconnect events)">

    /**
     * Runs when a client connects to the server
     *
     * @param s Server in question
     * @param c client that connected
     */
    @Override
    @Contract(mutates = "this")
    public void clientConnectionEvent(ModifiedServer s, ModifiedClient c) {
        newServer.onClientConnect(c);
    }

    /**
     * Runs when a client has disconnected - remove a client from the entry list
     *
     * @param c client that has just disconnected
     */
    @Override
    @Contract(mutates = "this")
    public void disconnectEvent(ModifiedClient c) {
        newServer.onClientDisconnect(c);
    }
    //</editor-fold>

    /**
     * @return true if there is a client message to process from the backlog
     */
    @Contract(pure = true)
    private boolean hasNewAction() {
        return !clientMessages.isEmpty();
    }

    /**
     * Starts the current game of hearts and transitions to "pass cards" stage
     */
    @Contract(mutates = "this")
    private void startGame() {
        gameState.startGame();
        newServer.onGameStart(gameState.getHandsInOrder());
    }

    /**
     * Asks the starting player to play the three of clubs
     *
     * @param starter player number to start
     */
    @Contract(mutates = "this")
    public void startFirstTurn(int starter) {
        newServer.startFirstTurn(starter, gameState.getHandsInOrder(), gameState.getPassingHands());
    }

    /**
     * Asks the next player to play a card
     *
     * @param justPlayed            player number that just played
     * @param playerNumOfNextPlayer player number of next person to player
     * @param played                Card played
     * @param required              Suit required
     */
    @Contract(mutates = "this")
    public void requestNextCard(int justPlayed, int playerNumOfNextPlayer, @NotNull Card played, @NotNull Suit required) {
        newServer.requestNextCard(justPlayed, playerNumOfNextPlayer, gameState.getCenter(), played, required);
    }

    /**
     * Starts a new turn and writes messages to players, given a "winner"
     *
     * @param winner         player number of winner from 1-4
     * @param addedPenalties penalties to add to his hand
     */
    @Contract(mutates = "this")
    public void startNewTurn(int winner, @NotNull Deck addedPenalties) {
        newServer.startNewTurn(winner, addedPenalties);
    }

    /**
     * When the game has endeed, write messages to players (i.e. who won, etc.)
     *
     * @param winner       winners of the game
     * @param points       points of each player
     * @param penaltyHands penalties of each player
     */
    @Contract(mutates = "this")
    public void endGame(boolean[] winner, int[] points, Deck[] penaltyHands) {
        newServer.endGame(winner, points, penaltyHands);
    }

    /**
     * Kicks a player due to an invalid message
     *
     * @param playerNum player num to kick
     */
    @Contract(mutates = "this")
    public void requestKickInvalidMessage(int playerNum) {
        newServer.kickInvalid(playerNum);

    }

    /**
     * Handles the play messages in the queue
     */
    @Contract(mutates = "this")
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

    /**
     * Resets the server/game
     */
    @Contract(mutates = "this")
    public void reset() {
        clientMessages.clear();
        newServer.reset();
        gameState.reset();
    }
}
