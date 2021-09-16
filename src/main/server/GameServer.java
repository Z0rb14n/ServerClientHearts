package server;

import net.MessagePair;
import net.ModifiedNewServer;
import net.message.client.ClientCardMessage;
import net.message.client.ClientThreeCardMessage;
import org.jetbrains.annotations.Contract;
import util.card.Card;
import util.card.Deck;

import java.util.ArrayDeque;

import static net.Constants.ERR_INVALID_MSG;

public class GameServer {
    private final ArrayDeque<MessagePair> clientMessages = new ArrayDeque<>();
    private final ModifiedNewServer newServer = new ModifiedNewServer();
    private final ServerGameState gameState = new ServerGameState();

    /**
     * @return access to the server
     */
    public ModifiedNewServer getNetServer() {
        return newServer;
    }

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
            // start game, transition to pass cards
            gameState.startGame();
            newServer.onGameStart(gameState.getHandsInOrder());
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
     * @return true if there is a client message to process from the backlog
     */
    @Contract(pure = true)
    private boolean hasNewAction() {
        return !clientMessages.isEmpty();
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
