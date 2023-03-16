package client.ui;

import client.ClientGameState;
import client.GameClient;
import client.console.Console;
import org.jetbrains.annotations.Contract;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

// Represents the first panel seen (i.e. to connect to the server)
class ConnectionPanel extends JPanel {
    private final static String TOO_MANY_PLAYERS_MSG = "Too many players.";
    private final static String CONNECTION_TIMEOUT = "Timed out.";
    private final static String DEFAULT_COULD_NOT_CONNECT = "Could not connect.";
    private static final Font font = new Font("Arial", Font.PLAIN, 24);
    private final IPEnterBox ipBox;
    private final JButton ipEnterButton = new JButton("Connect");
    private final JLabel errorDisplayer = new JLabel("");

    // EFFECTS: initializes connection panel with ip enter box and button
    ConnectionPanel() {
        super();
        setBorder(new EmptyBorder(30, 0, 0, 0));
        ipBox = new IPEnterBox();
        ipEnterButton.setFont(font);
        ipEnterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        ipEnterButton.addActionListener(e -> {
            System.out.print(ipBox.getText());
            ClientFrame.getFrame().tryLoadClient(ipBox.getText());
        });
    }

    // MODIFIES: this
    // EFFECTS: initializes this panel properly (must be called after constructor)
    void initialize() {
        setCorrectLayout();
        JLabel jl = new JLabel("Enter IP");
        jl.setAlignmentX(Component.CENTER_ALIGNMENT);
        jl.setFont(font);
        add(jl);
        add(ipBox);
        add(ipEnterButton);
        errorDisplayer.setForeground(Color.RED);
        errorDisplayer.setFont(font);
        errorDisplayer.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(errorDisplayer);
    }

    // MODIFIES: this
    // EFFECTS: sets the correct panel layout
    private void setCorrectLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    // MODIFIES: this
    // EFFECTS: updates the error to display
    void updateErrorDisplayed(String error) {
        errorDisplayer.setText(error);
        repaint();
    }

    /**
     * Attempts to load the client with given IP
     *
     * @param ip ip of client
     */
    @Contract(mutates = "this")
    public void attemptLoadClient(String ip) {
        updateErrorDisplayed("");
        GameClient.NetworkInstantiationResult result = GameClient.getInstance().connect(ip);
        switch (result) {
            case ALREADY_CONNECTED:
                if (ClientFrame.useConsole)
                    Console.getConsole().addMessage("Attempted to connect to client when one was already connected");
                break;
            case SUCCESS:
                if (ClientFrame.useConsole)
                    Console.getConsole().addMessage("Successful connection. Player num: " + ClientGameState.getInstance().getPlayerNumber()
                            + ", ID: " + GameClient.getInstance().getClientID());
                updateErrorDisplayed("");
                setVisible(false);
                ClientFrame.getFrame().switchToGamePanel();
                break;
            case TIMED_OUT:
                if (ClientFrame.useConsole)
                    Console.getConsole().addMessage("Connection timed out.");
                updateErrorDisplayed(CONNECTION_TIMEOUT);
                break;
            case KICKED:
                updateErrorDisplayed(TOO_MANY_PLAYERS_MSG);
                if (ClientFrame.useConsole) Console.getConsole().addMessage("Too many players.");
                break;
            default:
                updateErrorDisplayed(DEFAULT_COULD_NOT_CONNECT);
                if (ClientFrame.useConsole) Console.getConsole().addMessage("Could not connect.");
                break;
        }
    }

    // EFFECTS: gets the error displayed
    String getErrorDisplayed() {
        return errorDisplayer.getText();
    }

    // Represents the IP enter box
    private class IPEnterBox extends JTextField {
        // EFFECTS: initializes the ip enter box with given size
        IPEnterBox() {
            super(20);
            setFont(font);
            setAlignmentX(Component.CENTER_ALIGNMENT);
            addActionListener(e -> {
                // runs when they press the enter key
                if (ipBox.getText().length() != 0) {
                    ClientFrame.getFrame().tryLoadClient(ipBox.getText());
                }
            }); //when they press the enter key
            getDocument().addDocumentListener(new whenInputChanges());
            setMaximumSize(getPreferredSize());
        }

        // Represents a document listener that runs when the ip enter box contents is changed
        private class whenInputChanges implements DocumentListener {

            @Override
            public void insertUpdate(DocumentEvent e) {
                ipEnterButton.setEnabled(!ipBox.getText().isEmpty());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                ipEnterButton.setEnabled(!ipBox.getText().isEmpty());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                ipEnterButton.setEnabled(!ipBox.getText().isEmpty());
            }
        }
    }
}
