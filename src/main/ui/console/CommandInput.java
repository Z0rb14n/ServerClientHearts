package ui.console;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Represents the command input for the console
class CommandInput extends JPanel {
    private JTextField jtf = new JTextField(30);

    // EFFECTS: initializes the command input field
    CommandInput() {
        super();
        jtf.addActionListener(e -> sendCommand());
        add(jtf);
        add(new Button());
    }

    private void sendCommand() {
        Console.getConsole().runCommand(jtf.getText());
        jtf.setText("");
        repaint();
    }

    // Represents the button to submit the command
    private class Button extends JButton {
        // EFFECTS: initializes the button with given text and mouse listener
        Button() {
            super("Submit");
            addMouseListener(new Listener());
        }

        // Represent the mouse listener that would submit the command
        private class Listener extends MouseAdapter {
            @Override
            // EFFECTS: runs code when the submit button is pressed
            public void mouseClicked(MouseEvent e) {
                sendCommand();
            }
        }
    }
}
