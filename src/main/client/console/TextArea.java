package client.console;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Represents the main console text area
 */
class TextArea extends JPanel {
    private static final String COMMAND_PREFIX = "> ";
    private final JTextArea text = new JTextArea(15, 30);

    /**
     * Initializes text area with given margins and scroll area
     */
    TextArea() {
        super();
        text.setEditable(false);
        text.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane jsp = new JScrollPane(text);
        add(jsp);
        setVisible(true);
    }

    /**
     * Adds an executed command to the console window
     *
     * @param commandInput executed command
     */
    @Contract(mutates = "this")
    void addCommand(@NotNull String commandInput) {
        text.append(COMMAND_PREFIX + commandInput + "\n");
        repaint();
    }

    /**
     * Adds an output message to the console window
     *
     * @param input line of output to add
     */
    @Contract(mutates = "this")
    void addOutput(@NotNull String input) {
        text.append(input + "\n");
        repaint();
    }
}
