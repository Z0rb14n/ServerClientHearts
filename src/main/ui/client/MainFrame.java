package ui.client;

// TODO FINISH GUI

import net.*;
import ui.console.Console;
import util.ClientState;
import util.Deck;

import javax.swing.*;
import java.awt.*;

import static net.Constants.ERR_TIMED_OUT;
import static net.Constants.ERR_TOO_MANY_PLAYERS;

// Represents the main JFrame the user interacts with
public class MainFrame extends JFrame implements EventReceiver {
    private final static String TOO_MANY_PLAYERS_MSG = "Too many players.";
    private final static String CONNECTION_TIMEOUT = "Timed out.";
    private final static String DEFAULT_COULD_NOT_CONNECT = "Could not connect.";
    private final static Dimension WINDOW_DIMENSION = new Dimension(1366, 708);
    private boolean displayingInputIP = true;
    private GamePanel gp = new GamePanel();
    private ConnectionPanel cp = new ConnectionPanel();
    private ClientState clientState = new ClientState();
    private ModifiedNewClient client;
    private static MainFrame singleton;

    // MODIFIES: this
    // EFFECTS: ensures there is only one MainFrame in existence (see Singleton Design Pattern, Gang of Four)
    public static MainFrame getFrame() {
        if (singleton == null) {
            singleton = new MainFrame();
        }
        return singleton;
    }

    // EFFECTS: initializes the JFrame with an update updateTimer and a message receiver thread
    private MainFrame() {
        super("Server Client Hearts Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBackground(Color.WHITE);
        setPreferredSize(WINDOW_DIMENSION);
        setSize(WINDOW_DIMENSION);
        cp.initialize();
        add(cp);
        Console.getConsole(); // creates the console window
        pack();
        setVisible(true);
    }

    // MODIFIES: this
    // EFFECTS: plays the cards (and sends it to the server)
    // TODO METHOD BODY
    public void playCards(Deck d) {
        if (!isClientInactive()) {
            if (d.deckSize() == 1) client.write(ClientToServerMessage.createNewCardPlayedMessage(d.get(0)));
            else client.write(ClientToServerMessage.createNewSubmitThreeCardMessage(d));
        } else throw new IllegalArgumentException();
    }

    @Override
    public void dataReceivedEvent(ModifiedClient c) {
        System.out.println("EVENT: " + c.available() + "," + " is client inactive: " + isClientInactive());
        if (!isClientInactive()) {
            System.out.println(c instanceof ModifiedNewClient);
            ServerToClientMessage scm = client.readServerToClientMessage();
            assert (scm != null);
            if (!scm.isKickMessage()) {
                clientState.processNewMessage(scm);
                Console.getConsole().addMessage("New Message from Server: " + scm);
                gp.update();
            }
        }
        update();
    }

    @Override
    public void endOfStreamEvent(ModifiedClient c) {
        System.out.println("CLIENT RECEIVED END OF STREAM - KICK/HOST DISCONNECT.");
        if (!displayingInputIP) {
            invalidate();
            remove(gp);
            resetConnectionPanel();
            add(cp);
            clientState = new ClientState(); // reset
            displayingInputIP = true;
        }
        pack();
        repaint();
    }

    // MODIFIES: this
    // EFFECTS: resets the connection panel
    private void resetConnectionPanel() {
        cp = new ConnectionPanel();
        cp.initialize();
    }

    // MODIFIES: this
    // EFFECTS: updates the JFrame to indicate to show connection panel or game panel (currently unused)
    private void update() {
        if (isClientInactive() && !displayingInputIP) {
            remove(gp);
            resetConnectionPanel();
            add(cp);
            clientState = new ClientState(); // reset
            displayingInputIP = true;
        } else if (!isClientInactive() && displayingInputIP) {
            remove(cp);
            add(gp);
            displayingInputIP = false;
        }
        invalidate();
        pack();
        repaint();
    }

    // MODIFIES: this
    // EFFECTS: sends the chat message with given message to the server
    public void sendChatMessage(String message) {
        Console.getConsole().addMessage("Sending chat message: " + message);
        client.sendChatMessage(message);
    }

    // MODIFIES: this
    // EFFECTS: updates the error displayed on the jframe
    public void updateErrorMessage(String msg) {
        cp.updateErrorDisplayed(msg);
        add(cp);
        remove(gp);
        displayingInputIP = true;
        repaint();
    }

    // EFFECTS: returns the client state
    ClientState getClientState() {
        return clientState;
    }

    // MODIFIES: this
    // EFFECTS: feeds any messages from NewClient to clientState
    public void catchExtraMessages(ServerToClientMessage msg) {
        clientState.processNewMessage(msg);
    }

    // EFFECTS: determines whether the client is active
    private boolean isClientInactive() {
        return client == null || !client.active();
    }

    private volatile boolean didTimeout = true;
    private volatile boolean connectionSuccess = false;

    // Represents a thread to load the client to determine if it runs for too long
    private class ClientLoader extends Thread {
        private String loadedIP;

        // EFFECTS: initializes the client loader with the ip to attempt
        ClientLoader(String ip) {
            super();
            this.loadedIP = ip;
        }

        @Override
        // MODIFIES: this
        // EFFECTS: attempts to connect to the server, and if it does, indicate so
        public void run() {
            try {
                client = new ModifiedNewClient(MainFrame.getFrame(), loadedIP);
                didTimeout = false;
                connectionSuccess = true;
            } catch (ConnectionException e) {
                connectionSuccess = false;
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: attempts to load the client with given ip
    public void attemptLoadClient(String ip) {
        if (!isClientInactive()) {
            Console.getConsole().addMessage("Attempted to connect to client when one was already connected");
            return;
        }
        didTimeout = false;
        connectionSuccess = false;
        cp.updateErrorDisplayed("");
        boolean failed = false;
        try {
            ClientLoader cl = new ClientLoader(ip);
            cl.start();
            cl.join(10000);
            if (client == null || didTimeout || !connectionSuccess) {
                failed = true;
                Console.getConsole().addMessage("Connection timed out.");
                cp.updateErrorDisplayed(CONNECTION_TIMEOUT);
            } else {
                clientState.setPlayerNum(client.getPlayerNum());
                Console.getConsole().addMessage("Successful connection. Player num: " + client.getPlayerNum() + ", ID: " + client.getClientID());
            }
        } catch (ConnectionException e) {
            if (e.getMessage().equals(ERR_TIMED_OUT)) {
                cp.updateErrorDisplayed(CONNECTION_TIMEOUT);
                Console.getConsole().addMessage("Connection timeout.");
            } else if (e.getMessage().equals(ERR_TOO_MANY_PLAYERS)) {
                cp.updateErrorDisplayed(TOO_MANY_PLAYERS_MSG);
                Console.getConsole().addMessage("Too many players.");
            }
            failed = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(2);
        }
        if (client == null) {
            if (cp.getErrorDisplayed().equals("")) {
                cp.updateErrorDisplayed(DEFAULT_COULD_NOT_CONNECT);
                Console.getConsole().addMessage("Could not connect.");
            }
        } else if (!failed) {
            cp.updateErrorDisplayed("");
            cp.setVisible(false);
            remove(cp);
            add(gp);
            invalidate();
            displayingInputIP = false;
            repaint();
        }
    }
}
