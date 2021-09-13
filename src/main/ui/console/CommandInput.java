package ui.console;

import org.jetbrains.annotations.Contract;

import javax.swing.*;

/**
 * Represents the command input + button for the console
 */
class CommandInput extends JPanel {
    private final JTextField jtf = new JTextField(30);

    /**
     * Initializes command input field and submit button
     */
    CommandInput() {
        super();
        jtf.addActionListener(e -> sendCommand());
        add(jtf);
        JButton button = new JButton("Submit");
        button.addActionListener(e -> sendCommand());
        add(button);
    }

    /**
     * Runs a command and clears the text in the input field
     */
    @Contract(mutates = "this")
    private void sendCommand() {
        Console.getConsole().runCommand(jtf.getText());
        jtf.setText("");
        repaint();
    }
}
