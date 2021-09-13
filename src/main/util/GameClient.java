package util;

import net.ConnectionException;
import net.Constants;
import net.ModifiedClient;
import net.ModifiedNewClient;
import net.message.client.ClientCardMessage;
import net.message.client.ClientChatMessage;
import net.message.client.ClientThreeCardMessage;
import net.message.server.*;
import ui.client.MainFrame;
import ui.console.Console;
import util.card.Card;
import util.card.Deck;
import util.card.Suit;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static net.Constants.DEFAULT_TIMEOUT;

public class GameClient implements net.ObjectEventReceiver {
    public static final int MAX_CHAT_MESSAGES = 100;
    private final ClientGameState gameState = new ClientGameState();
    private final Queue<ChatMessage> chatMessages = new ArrayDeque<>(MAX_CHAT_MESSAGES);
    private final boolean[] onlinePlayers = new boolean[4];
    private final ReentrantLock waitingForMessageMutex = new ReentrantLock();
    private final Condition messageReceivedCondition = waitingForMessageMutex.newCondition();
    private ModifiedNewClient networkClient;

    //<editor-fold desc="Singleton Design Pattern">
    private static GameClient instance;

    public static GameClient getInstance() {
        if (instance == null) instance = new GameClient();
        return instance;
    }
    //</editor-fold>

    public GameClient() {
        ClientLogger.logMessage("Initialized game client object.");
    }

    //<editor-fold desc="Network Client Instantiation">
    private NetworkInstantiationResult resultFromThread = null;

    /**
     * Attempts to connect to a server with given ip and default timeout.
     *
     * @param ip IP address of server.
     * @return NetworkInstantiationResult (i.e. whether it failed)
     */
    public NetworkInstantiationResult connect(String ip) {
        return connect(DEFAULT_TIMEOUT, ip);
    }

    /**
     * Attempts to connect to a server with given ip and timeout.
     *
     * @param timeout timeout in milliseconds to wait for a connection.
     * @param ip      IP address of server.
     * @return NetworkInstantiationResult (i.e. whether it failed)
     */
    public NetworkInstantiationResult connect(long timeout, String ip) {
        resultFromThread = null;
        try {
            ClientLogger.logMessage("Starting instantiation of client...");
            NetworkInstantiationThread thread = new NetworkInstantiationThread(Thread.currentThread(), ip);
            thread.start();
            thread.join(timeout);
            ClientLogger.logMessage("Timeout reached.");
            thread.interrupt();
            if (resultFromThread != null) return resultFromThread;
            return NetworkInstantiationResult.TIMED_OUT;
        } catch (InterruptedException ex) {
            ClientLogger.logMessage("Thread interrupted. Reading response.");
            if (resultFromThread != null) return resultFromThread;
            else return NetworkInstantiationResult.FAILED;
        }
    }

    /**
     * Thread that interrupts a thread upon connection completion.
     */
    private class NetworkInstantiationThread extends Thread {
        private final Thread thread;
        private final String ip;

        public NetworkInstantiationThread(Thread threadToInterrupt, String ip) {
            this.thread = threadToInterrupt;
            this.ip = ip;
        }

        /**
         * Gets client ID from the server.
         *
         * @throws ConnectionException if kicked from the server
         * @throws RuntimeException    if interrupted when waiting for a ClientID message
         */
        private boolean getClientIDFromServer() {
            waitingForMessageMutex.lock();
            if (networkClient.numMessages() == 0) {
                try {
                    messageReceivedCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted upon waiting for clientID message.");
                }
            }
            assert (networkClient.numMessages() > 0);
            ServerToClientMessage msg = networkClient.removeServerToClientMessage();
            assert (msg != null);
            waitingForMessageMutex.unlock();
            if (msg instanceof ServerKickMessage) {
                return false;
            } else if (msg instanceof ServerIDMessage) {
                ServerIDMessage idMessage = (ServerIDMessage) msg;
                gameState.onGetID(idMessage.getID(), idMessage.getPlayerNumber());
                return true;
            } else {
                System.out.println("[GameClient::getClientIDFromServer]: WARNING: did not receive ID message. Returning false");
                return false;
            }
        }

        @Override
        public void run() {
            if (networkClient != null && networkClient.active())
                resultFromThread = NetworkInstantiationResult.ALREADY_CONNECTED;
            try {
                networkClient = new ModifiedNewClient(GameClient.this, ip);
                boolean result = getClientIDFromServer();
                if (result) resultFromThread = NetworkInstantiationResult.SUCCESS;
                else resultFromThread = NetworkInstantiationResult.KICKED;
            } catch (ConnectionException ex) {
                if (Constants.isTimeoutMessage(ex.getMessage()))
                    resultFromThread = NetworkInstantiationResult.TIMED_OUT;
                else resultFromThread = NetworkInstantiationResult.FAILED;
            }
            thread.interrupt();
        }
    }

    public enum NetworkInstantiationResult {
        SUCCESS, TIMED_OUT, KICKED, FAILED, ALREADY_CONNECTED
    }
    //</editor-fold>

    public boolean isClientActive() {
        return networkClient != null && networkClient.active();
    }

    private boolean checkClientActive() {
        if (networkClient == null) {
            ClientLogger.logMessage("[CheckClientActive]: [ERROR] - network client is null.");
            return false;
        }
        if (!networkClient.active()) {
            ClientLogger.logMessage("[CheckClientActive]: [ERROR] - network client is not active.");
            return false;
        }
        return true;
    }

    public boolean[] getOnlinePlayers() {
        boolean[] array = new boolean[4];
        System.arraycopy(onlinePlayers, 0, array, 0, 4);
        return array;
    }

    public String getClientID() {
        if (networkClient == null) return null;
        return networkClient.getClientID();
    }

    public boolean playCard(Card c) {
        ClientLogger.logMessage("Playing card " + c + "...");
        boolean result = checkClientActive();
        if (!result) return false;
        if (gameState.isValidCardPlay(c)) {
            ClientLogger.logMessage("Attempting to send card " + c.toString());
            networkClient.write(new ClientCardMessage(c));
            return true;
        }
        ClientLogger.logMessage("[ERROR]: cannot play card " + c.toString() + ": it is not a valid card play.");
        return false;
    }

    public boolean passCards(Card cardOne, Card cardTwo, Card cardThree) {
        ClientLogger.logMessage("Passing cards " + cardOne + "," + cardTwo + "," + cardThree + "...");
        boolean result = checkClientActive();
        if (!result) return false;
        if (gameState.isValidCardPass(cardOne, cardTwo, cardThree)) {
            networkClient.write(new ClientThreeCardMessage(cardOne, cardTwo, cardThree));
            ClientLogger.logMessage("Passed cards!");
            gameState.playerDeck.remove(cardOne);
            gameState.playerDeck.remove(cardTwo);
            gameState.playerDeck.remove(cardThree);
            MainFrame.getFrame().update();
            return true;
        }
        ClientLogger.logMessage("[ERROR]: cannot pass cards - it contains invalid card(s).");
        ClientLogger.logMessage(gameState.playerDeck.toString());
        return false;
    }

    public boolean sendChatMessage(String msg) {
        ClientLogger.logMessage("Sending chat message " + msg + "...");
        boolean result = checkClientActive();
        if (!result) return false;
        networkClient.write(new ClientChatMessage(msg));
        return true;
    }

    public List<ChatMessage> getChatMessages() {
        return Collections.unmodifiableList(new ArrayList<>(chatMessages));
    }

    public void reset() {
        gameState.reset();
        for (int i = 0; i < 4; i++) onlinePlayers[i] = false;
        chatMessages.clear();
    }

    public ClientGameState getClientState() {
        return gameState;
    }

    //<editor-fold desc="Incoming Message Processing">

    public void processIncomingMessage(ServerToClientMessage message) {
        if (message instanceof ServerRequestNextCardMessage) {
            handleNextCardMessage((ServerRequestNextCardMessage) message);
        } else if (message instanceof ServerChatMessage) {
            handleNewChatMessage((ServerChatMessage) message);
        } else if (message instanceof ServerIDMessage || message instanceof ServerPlayerDisconnectionMessage || message instanceof ServerPlayerConnectionMessage) {
            handleOnlinePlayerUpdateMessage(message);
        } else if (message instanceof ServerStartGameMessage) {
            handleGameStartMessages((ServerStartGameMessage) message);
        } else if (message instanceof ServerStartFirstTurnMessage) {
            handleStartFirstTurnMessage((ServerStartFirstTurnMessage) message);
        } else if (message instanceof ServerNextTurnMessage) {
            handleNewTurnMessage((ServerNextTurnMessage) message);
        } else if (message instanceof ServerGameEndMessage) {
            handleGameEndMessage((ServerGameEndMessage) message);
        }
    }

    private void handleNextCardMessage(ServerRequestNextCardMessage msg) {
        gameState.onCardPlay(msg.getPreviouslyPlayed(), msg.getPlayerNumJustPlayed(), msg.getNextPlayerNumber(), msg.getExpectedSuit());
    }

    private void handleStartFirstTurnMessage(ServerStartFirstTurnMessage msg) {
        gameState.finishPassingCards(msg);
    }

    private void handleGameEndMessage(ServerGameEndMessage msg) {
        gameState.onGameEnd(msg.getWinners(), msg.getPenaltyHands());
    }

    // MODIFIES: this
    // EFFECTS: handles player chat message
    private void handleNewChatMessage(ServerChatMessage msg) {
        ChatMessage cm = new ChatMessage(msg.getPlayerNumber(), msg.getMessage());
        if (chatMessages.size() == MAX_CHAT_MESSAGES) {
            ClientLogger.logMessage("[WARN]: Removed chat message " + chatMessages.remove());
        }
        chatMessages.add(cm);
        MainFrame.getFrame().addChatMessage(cm);
    }

    // MODIFIES: this
    // EFFECTS: handles new player addition/removal messages (includes ID message)
    private boolean handleOnlinePlayerUpdateMessage(ServerToClientMessage msg) {
        int num;
        if (msg instanceof ServerIDMessage) {
            ServerIDMessage idMessage = (ServerIDMessage) msg;
            System.arraycopy(idMessage.getExistingPlayers(), 0, onlinePlayers, 0, 4);
            gameState.setPlayerNumber(idMessage.getPlayerNumber());
            onlinePlayers[idMessage.getPlayerNumber() - 1] = true; // just to be sure
            return true;
        } else if (msg instanceof ServerPlayerConnectionMessage) {
            ServerPlayerConnectionMessage playerConnectionMessage = (ServerPlayerConnectionMessage) msg;
            num = playerConnectionMessage.getNewPlayerNumber();
            onlinePlayers[num - 1] = true;
            return true;
        } else if (msg instanceof ServerPlayerDisconnectionMessage) {
            ServerPlayerDisconnectionMessage playerDisconnectionMessage = (ServerPlayerDisconnectionMessage) msg;
            num = playerDisconnectionMessage.getPlayerNumber();
            onlinePlayers[num - 1] = false;
            return true;
        }
        return false;
    }

    private void handleNewTurnMessage(ServerNextTurnMessage msg) {
        gameState.onNextTurn(msg.getNewPenaltyCards(), msg.getPlayerWhoStartsNext());
    }

    // MODIFIES: this
    // EFFECTS: handles gameStarting messages
    private void handleGameStartMessages(ServerStartGameMessage msg) {
        gameState.startGame(msg.getClientHand().copy());
    }

    //</editor-fold>

    @Override
    public void dataReceivedEvent(ModifiedClient c, Object o) {
        assert (o instanceof ServerToClientMessage);
        waitingForMessageMutex.lock();
        messageReceivedCondition.signal();
        waitingForMessageMutex.unlock();
        ServerToClientMessage message = (ServerToClientMessage) o;
        processIncomingMessage(message);
        Console.getConsole().addMessage("New Message from Server: " + message);
        MainFrame.getFrame().update();
    }

    @Override
    public void endOfStreamEvent(ModifiedClient c) {
        System.out.println("CLIENT RECEIVED END OF STREAM - KICK/HOST DISCONNECT.");
        MainFrame.getFrame().switchToDisplayIPInput();
        reset();
    }

    public static class ClientLogger {
        private static final StringBuilder stringBuilder = new StringBuilder();
        private static final ArrayList<String> messages = new ArrayList<>(100);

        public static List<String> getMessages() {
            return Collections.unmodifiableList(messages);
        }

        public static void logMessage(String message) {
            stringBuilder.append(message).append("\n");
            messages.add(message);
            System.out.println(message);
        }

        public static String getLoggerText() {
            return stringBuilder.toString();
        }
    }

    public static class ClientGameState implements Serializable {
        private static final long serialVersionUID = 69420L;

        private String clientID;
        private int playerNumber = -1;
        private PlayOrder currentPlayOrder = null;
        private PassOrder currentPassOrder = null;
        private final Deck center = new Deck();
        private Suit requiredSuitToPlay = null;
        private final Deck receivedPassingHand = new Deck();
        private Deck playerDeck = new Deck();
        private final Deck[] penalties = new Deck[4];
        private final int[] numCardsRemaining = new int[4];
        private final int[] numCardsInPassingHand = new int[4];
        private final boolean[] winners = new boolean[4];
        private final boolean[] playersThatPassed = new boolean[4];
        private boolean gameStarted = false;
        private boolean gameEnded = false;
        private boolean startedPassingCards = false;
        private boolean allCardsPassed = false;
        private int nextToPlay = -1;
        private int firstPlayed = -1;
        private int numRoundsPlayed = 0;

        public ClientGameState() {
            reset();
        }

        public void reset() {
            currentPlayOrder = null;
            gameEnded = false;
            requiredSuitToPlay = null;
            currentPassOrder = null;
            center.clear();
            receivedPassingHand.clear();
            playerDeck.clear();
            for (int i = 0; i < 4; i++) {
                penalties[i] = new Deck();
                numCardsRemaining[i] = 13;
                numCardsInPassingHand[i] = 0;
                winners[i] = false;
                playersThatPassed[i] = false;
            }
            gameStarted = false;
            startedPassingCards = false;
            allCardsPassed = false;
            nextToPlay = -1;
            firstPlayed = -1;
            numRoundsPlayed = 0;
        }

        private void startGame(Deck startingDeck) {
            playerDeck = startingDeck.copy();
            gameStarted = true;
            startedPassingCards = true;
            ClientLogger.logMessage("Player hand: " + startingDeck);
        }

        private void finishPassingCards(ServerStartFirstTurnMessage ignored) {
            allCardsPassed = true;
        }

        private void onNextTurn(Deck newPenalties, int player) {
            penalties[player - 1].add(newPenalties);
            nextToPlay = player;
            requiredSuitToPlay = null;
        }

        private void onCardPlay(Card prevPlayed, int justPlayed, int nextToPlay, Suit requiredSuit) {
            this.nextToPlay = nextToPlay;
            if (firstPlayed == -1) firstPlayed = justPlayed;
            requiredSuitToPlay = requiredSuit;
            center.add(prevPlayed);
        }

        private void setPlayerNumber(int number) {
            this.playerNumber = number;
        }

        private void onGetID(String ID, int number) {
            this.playerNumber = number;
            this.clientID = ID;
        }

        private void onGameEnd(boolean[] winners, Deck[] penalties) {
            gameEnded = true;
            System.arraycopy(winners, 0, this.winners, 0, 4);
            System.arraycopy(penalties, 0, this.penalties, 0, 4);
        }

        public boolean hasGameEnded() {
            return gameEnded;
        }

        public boolean[] getWinners() {
            assert (gameEnded);
            return winners;
        }

        public boolean isValidCardPass(Card c1, Card c2, Card c3) {
            return startedPassingCards && !allCardsPassed && playerDeck.contains(c1) && playerDeck.contains(c2) && playerDeck.contains(c3);
        }

        public boolean isValidCardPlay(Card c) {
            return canPlay() && playerDeck.contains(c);
        }

        public boolean canPlay() {
            return playerNumber == nextToPlay;
        }

        public boolean shouldPlayThreeOfClubs() {
            return center.size() == 0 && numRoundsPlayed == 0;
        }

        public int getPlayerNumber() {
            return playerNumber;
        }

        public PlayOrder getCurrentPlayOrder() {
            return currentPlayOrder;
        }

        public PassOrder getCurrentPassOrder() {
            return currentPassOrder;
        }

        public Deck getCenter() {
            return center.copy();
        }

        public Deck getReceivedPassingHand() {
            return receivedPassingHand.copy();
        }

        public Deck getPlayerDeck() {
            return playerDeck.copy();
        }

        public Deck[] getPenalties() {
            Deck[] decks = new Deck[4];
            for (int i = 0; i < 4; i++) {
                decks[i] = penalties[i].copy();
            }
            return decks;
        }

        public int[] getNumCardsRemaining() {
            int[] array = new int[4];
            System.arraycopy(numCardsRemaining, 0, array, 0, 4);
            return array;
        }

        public int[] getNumCardsInPassingHand() {
            int[] array = new int[4];
            System.arraycopy(numCardsInPassingHand, 0, array, 0, 4);
            return array;
        }

        public boolean isGameStarted() {
            return gameStarted;
        }

        public boolean isStartedPassingCards() {
            return startedPassingCards;
        }

        public boolean isAllCardsPassed() {
            return allCardsPassed;
        }

        public int getNextToPlay() {
            return nextToPlay;
        }

        public int getFirstPlayed() {
            return firstPlayed;
        }

        public int getNumRoundsPlayed() {
            return numRoundsPlayed;
        }
    }
}