package client.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Represents the first panel seen (i.e. to connect to the server)
class ConnectionPanel extends JPanel {
    private static final Font font = new Font("Arial", Font.PLAIN, 24);
    private final IPEnterBox ipBox;
    private final IPEnterButton ipEnterButton;
    private final JLabel errorDisplayer = new JLabel("");

    // EFFECTS: initializes connection panel with ip enter box and button
    ConnectionPanel() {
        super();
        setBorder(new EmptyBorder(30, 0, 0, 0));
        ipBox = new IPEnterBox();
        ipEnterButton = new IPEnterButton();
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
            addActionListener(new onFinalizedInput()); //when they press the enter key
            getDocument().addDocumentListener(new whenInputChanges());
            setMaximumSize(getPreferredSize());
        }

        // Represents an action listener to be run when the user presses the action key
        private class onFinalizedInput implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ipBox.getText().length() != 0) {
                    MainFrame.getFrame().attemptLoadClient(ipBox.getText());
                }
            }
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

    private class IPEnterButton extends JButton {
        IPEnterButton() {
            super("Connect");
            setFont(font);
            setAlignmentX(Component.CENTER_ALIGNMENT);
            addMouseListener(new Listener());
        }

        private class Listener extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    System.out.print(ipBox.getText());
                    MainFrame.getFrame().attemptLoadClient(ipBox.getText());
                }
            }
        }
    }
}
