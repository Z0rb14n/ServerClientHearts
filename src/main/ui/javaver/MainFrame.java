package ui.javaver;

// TODO EXPERIMENTAL JFRAME VERSION

import net.ConnectionException;
import net.NewClient;
import net.ServerToClientMessage;
import ui.console.Console;
import util.ClientState;

import javax.swing.*;
import java.awt.*;

import static net.Constants.ERR_TIMED_OUT;
import static net.Constants.ERR_TOO_MANY_PLAYERS;

public class MainFrame extends JFrame {
    private final static String TOO_MANY_PLAYERS_MSG = "Too many players.";
    private final static String CONNECTION_TIMEOUT = "Timed out.";
    private final static String DEFAULT_COULD_NOT_CONNECT = "Could not connect.";
    private final static Dimension WINDOW_DIMENSION = new Dimension(1366, 708);
    private boolean displayingInputIP = true;
    private ConnectionPanel cp = new ConnectionPanel();
    private ClientState clientState = new ClientState();
    private NewClient client;
    private static MainFrame singleton;

    public static MainFrame getFrame() {
        if (singleton == null) {
            singleton = new MainFrame();
        }
        return singleton;
    }

    private MainFrame() {
        super("Server Client Hearts Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBackground(Color.WHITE);
        setPreferredSize(WINDOW_DIMENSION);
        setSize(WINDOW_DIMENSION);
        cp.initialize();
        add(cp);
        setVisible(true);
    }

    private void update() {
        if (isClientInactive() && !displayingInputIP) {
            add(cp);
            displayingInputIP = true;
        } else if (!isClientInactive() && displayingInputIP) {
            remove(cp);
            displayingInputIP = false;
        }
        repaint();
    }

    public void updateErrorMessage(String msg) {
        cp.updateErrorDisplayed(msg);
        add(cp);
        displayingInputIP = true;
        repaint();
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

    private boolean didTimeout = true;

    private class ClientLoader extends Thread {
        private String loadedIP;

        ClientLoader(String ip) {
            super();
            this.loadedIP = ip;
        }

        @Override
        public void run() {
            client = new NewClient(loadedIP, true);
            didTimeout = false;
        }
    }

    // MODIFIES: this
    // EFFECTS: attempts to load the client with given ip
    void attemptLoadClient(String ip) {
        if (!isClientInactive()) {
            Console.getConsole().addMessage("Attempted to connect to client when one was already connected");
            return;
        }
        cp.updateErrorDisplayed("");
        boolean failed = false;
        try {
            ClientLoader cl = new ClientLoader(ip);
            cl.start();
            cl.join(10000);
            if (client == null || didTimeout) {
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
        }
        update();
    }
}
