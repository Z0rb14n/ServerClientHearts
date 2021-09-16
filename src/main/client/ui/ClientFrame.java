package client.ui;

// TODO FINISH GUI

import client.GameClient;
import client.console.Console;
import util.ChatMessage;

import javax.swing.*;
import java.awt.*;

/**
 * Main JFrame the user interacts with on the client
 */
public class ClientFrame extends JFrame {
    public static boolean useConsole = true;
    private final static Dimension WINDOW_DIMENSION = new Dimension(1366, 708);
    private boolean displayingInputIP = true;
    private final GamePanel gp = new GamePanel();
    private ConnectionPanel cp = new ConnectionPanel();
    private static ClientFrame singleton;

    // MODIFIES: this
    // EFFECTS: ensures there is only one MainFrame in existence
    public static ClientFrame getFrame() {
        if (singleton == null) {
            singleton = new ClientFrame();
        }
        return singleton;
    }

    // EFFECTS: initializes the JFrame with an update updateTimer and a message receiver thread
    private ClientFrame() {
        super("Server Client Hearts Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBackground(Color.WHITE);
        setPreferredSize(WINDOW_DIMENSION);
        setSize(WINDOW_DIMENSION);
        cp.initialize();
        add(cp);
        if (useConsole)
            Console.getConsole(); // creates the console window
        pack();
        setVisible(true);
    }

    public void switchToGamePanel() {
        if (displayingInputIP) {
            invalidate();
            remove(cp);
            add(gp);
            displayingInputIP = false;
            gp.update();
            validate();
        }
        pack();
        repaint();
    }

    public void switchToDisplayIPInput() {
        if (!displayingInputIP) {
            invalidate();
            remove(gp);
            resetConnectionPanel();
            add(cp);
            GameClient.getInstance().reset();
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
    public void update() {
        if (!GameClient.getInstance().isClientActive() && !displayingInputIP) {
            switchToDisplayIPInput();
        } else if (GameClient.getInstance().isClientActive() && displayingInputIP) {
            switchToGamePanel();
        } else if (!displayingInputIP) {
            gp.update();
            repaint();
        }
    }

    /**
     * Adds a chat message to the game panel.
     *
     * @param msg chat message to be added
     */
    public void addChatMessage(ChatMessage msg) {
        gp.addNewChatMessage(msg);
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

    public void tryLoadClient(String ip) {
        if (displayingInputIP) {
            cp.attemptLoadClient(ip);
        }
    }
}
