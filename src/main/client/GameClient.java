package client;

import client.console.Console;
import client.ui.ClientFrame;
import net.*;
import net.message.client.ClientCardMessage;
import net.message.client.ClientChatMessage;
import net.message.client.ClientThreeCardMessage;
import net.message.server.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import util.ChatMessage;
import util.card.Card;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static net.Constants.DEFAULT_TIMEOUT;

public class GameClient implements ObjectEventReceiver {
    public static final int MAX_CHAT_MESSAGES = 100;
    private final ClientGameState gameState = new ClientGameState();
    private final Queue<ChatMessage> chatMessages = new ArrayDeque<>(MAX_CHAT_MESSAGES);
    private final boolean[] onlinePlayers = new boolean[4];
    private final ReentrantLock waitingForMessageMutex = new ReentrantLock();
    private final Condition messageReceivedCondition = waitingForMessageMutex.newCondition();
    private ModifiedNewClient networkClient;

    ///<editor-fold desc="Singleton Design Pattern">
    private static GameClient instance;

    public static GameClient getInstance() {
        if (instance == null) instance = new GameClient();
        return instance;
    }
    ///</editor-fold>

    public GameClient() {
        ClientLogger.logMessage("Initialized game client object.");
    }

    ///<editor-fold desc="Network Client Instantiation">
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
                handleServerIDMessage((ServerIDMessage) msg);
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
    ///</editor-fold>

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
            gameState.playerDeck.remove(c);
            ClientFrame.getFrame().update();
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
            ClientFrame.getFrame().update();
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
            gameState.onCardPlay((ServerRequestNextCardMessage) message);
        } else if (message instanceof ServerChatMessage) {
            handleNewChatMessage((ServerChatMessage) message);
        } else if (message instanceof ServerPlayerDisconnectionMessage) {
            handleServerPlayerDisconnectionMessage((ServerPlayerDisconnectionMessage) message);
        } else if (message instanceof ServerStartGameMessage) {
            gameState.startGame((ServerStartGameMessage) message);
        } else if (message instanceof ServerStartFirstTurnMessage) {
            gameState.finishPassingCards((ServerStartFirstTurnMessage) message);
        } else if (message instanceof ServerNextTurnMessage) {
            gameState.onNextTurn((ServerNextTurnMessage) message);
        } else if (message instanceof ServerGameEndMessage) {
            gameState.onGameEnd((ServerGameEndMessage) message);
        } else if (message instanceof ServerIDMessage) {
            handleServerIDMessage((ServerIDMessage) message);
        } else if (message instanceof ServerPlayerConnectionMessage) {
            handleServerPlayerConnectionMessage((ServerPlayerConnectionMessage) message);
        } else if (message instanceof ServerGameResetMessage) {
            gameState.onGameReset((ServerGameResetMessage) message);
            reset();
        }
        ClientFrame.getFrame().update();
    }

    // MODIFIES: this
    // EFFECTS: handles player chat message
    private void handleNewChatMessage(ServerChatMessage msg) {
        ChatMessage cm = new ChatMessage(msg.getPlayerNumber(), msg.getMessage());
        if (chatMessages.size() == MAX_CHAT_MESSAGES) {
            ClientLogger.logMessage("[WARN]: Removed chat message " + chatMessages.remove());
        }
        chatMessages.add(cm);
        ClientFrame.getFrame().addChatMessage(cm);
    }

    /**
     * Handles the initial ID message and initial player information
     *
     * @param idMessage Sent ServerIDMessage
     */
    @Contract(mutates = "this")
    public void handleServerIDMessage(@NotNull ServerIDMessage idMessage) {
        gameState.onIDMessage(idMessage);
        System.arraycopy(idMessage.getExistingPlayers(), 0, onlinePlayers, 0, 4);
        onlinePlayers[idMessage.getPlayerNumber() - 1] = true; // just to be sure
    }

    /**
     * Handles a single player addition message
     *
     * @param playerConnectionMessage message sent by the server
     */
    @Contract(mutates = "this")
    private void handleServerPlayerConnectionMessage(@NotNull ServerPlayerConnectionMessage playerConnectionMessage) {
        onlinePlayers[playerConnectionMessage.getNewPlayerNumber() - 1] = true;
    }

    /**
     * Handles a single player disconnection message
     *
     * @param disconnectMessage message sent by the server
     */
    @Contract(mutates = "this")
    private void handleServerPlayerDisconnectionMessage(@NotNull ServerPlayerDisconnectionMessage disconnectMessage) {
        onlinePlayers[disconnectMessage.getPlayerNumber() - 1] = false;
    }
    //</editor-fold>

    @Override
    public void dataReceivedEvent(ModifiedClient c, Object o) {
        assert (o instanceof ServerToClientMessage);
        waitingForMessageMutex.lock();
        messageReceivedCondition.signal();
        waitingForMessageMutex.unlock();
        ServerToClientMessage message = (ServerToClientMessage) o;
        if (ClientFrame.useConsole)
            Console.getConsole().addMessage("New Message from Server: " + message);
        processIncomingMessage(message);
        ClientFrame.getFrame().update();
    }

    @Override
    public void endOfStreamEvent(ModifiedClient c) {
        System.out.println("CLIENT RECEIVED END OF STREAM - KICK/HOST DISCONNECT.");
        ClientFrame.getFrame().switchToDisplayIPInput();
        reset();
    }

}
