package util;

import net.ClientToServerMessage;
import net.ConnectionException;
import net.Constants;
import net.ModifiedNewClient;
import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.*;

import static net.Constants.DEFAULT_TIMEOUT;

public class GameClient implements net.EventReceiver {
    public static final int MAX_CHAT_MESSAGES = 100;
    private final ClientGameState gameState = new ClientGameState();
    private final Queue<ChatMessage> chatMessages = new ArrayDeque<>(MAX_CHAT_MESSAGES);
    private final boolean[] onlinePlayers = new boolean[4];
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

        @Override
        public void run() {
            try {
                networkClient = new ModifiedNewClient(GameClient.this, ip);
                resultFromThread = NetworkInstantiationResult.SUCCESS;
            } catch (ConnectionException ex) {
                if (Constants.isTimeoutMessage(ex.getMessage())) {
                    resultFromThread = NetworkInstantiationResult.TIMED_OUT;
                } else if (Constants.isKickMessage(ex.getMessage())) {
                    resultFromThread = NetworkInstantiationResult.KICKED;
                } else resultFromThread = NetworkInstantiationResult.FAILED;
            }
            thread.interrupt();
        }
    }

    public enum NetworkInstantiationResult {
        SUCCESS, TIMED_OUT, KICKED, FAILED
    }
    //</editor-fold>

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

    public boolean playCard(Card c) {
        ClientLogger.logMessage("Playing card " + c + "...");
        boolean result = checkClientActive();
        if (!result) return false;
        if (gameState.isValidCardPlay(c)) {
            ClientLogger.logMessage("Attempting to send card " + c.toString());
            networkClient.write(ClientToServerMessage.createNewCardPlayedMessage(c));
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
            ClientLogger.logMessage("Attempting to pass cards...");
            networkClient.write(ClientToServerMessage.createNewSubmitThreeCardMessage(cardOne, cardTwo, cardThree));
            return true;
        }
        ClientLogger.logMessage("[ERROR]: cannot pass cards - it contains invalid card(s).");
        return false;
    }

    public boolean sendChatMessage(String msg) {
        ClientLogger.logMessage("Sending chat message " + msg + "...");
        boolean result = checkClientActive();
        if (!result) return false;
        networkClient.write(ClientToServerMessage.createNewChatMessage(msg));
        return true;
    }

    public void reset() {
        gameState.reset();
        for (int i = 0; i < 4; i++) onlinePlayers[i] = false;
        chatMessages.clear();
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

        @Contract(pure = true)
        public static String getLoggerText() {
            return stringBuilder.toString();
        }
    }

    private static class ClientGameState implements Serializable {
        private static final long serialVersionUID = 69420L;

        public int playerNumber = -1;
        public PlayOrder currentPlayOrder = null;
        public PassOrder currentPassOrder = null;
        public Deck center = new Deck();
        public Deck receivedPassingHand = new Deck();
        public Deck playerDeck = new Deck();
        public final Deck[] penalties = new Deck[4];
        public final int[] numCardsRemaining = new int[4];
        public final int[] numCardsInPassingHand = new int[4];
        public boolean hasGameStarted = false;
        public boolean hasStartedPassingCards = false;
        public boolean hasAllCardsPassed = false;
        public int nextToPlay = -1;
        public int firstPlayed = -1;
        public int numRoundsPlayed = 0;

        public ClientGameState() {
            reset();
        }

        public void reset() {
            currentPlayOrder = null;
            currentPassOrder = null;
            center.clear();
            receivedPassingHand.clear();
            playerDeck.clear();
            for (int i = 0; i < 4; i++) {
                penalties[i] = new Deck();
                numCardsRemaining[i] = 13;
                numCardsInPassingHand[i] = 0;
            }
            hasGameStarted = false;
            hasStartedPassingCards = false;
            hasAllCardsPassed = false;
            nextToPlay = -1;
            firstPlayed = -1;
            numRoundsPlayed = 0;
        }

        public boolean isValidCardPass(Card c1, Card c2, Card c3) {
            return hasStartedPassingCards && !hasAllCardsPassed && playerDeck.contains(c1) && playerDeck.contains(c2) && playerDeck.contains(c3);
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
    }
}
