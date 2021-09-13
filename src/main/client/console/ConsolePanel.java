package client.console;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Represents the main console view panel
 */
class ConsolePanel extends JPanel {
    private final TextArea ta;

    /**
     * Initializes console panel with correct layout and text area
     */
    ConsolePanel() {
        super();
        setLayout(new BorderLayout());
        ta = new TextArea();
        add(ta, BorderLayout.CENTER);
        add(new CommandInput(), BorderLayout.PAGE_END);
        setVisible(true);
    }

    /**
     * Adds a command to the console panel/text area
     *
     * @param input line to add
     */
    @Contract(mutates = "this")
    void addCommand(@NotNull String input) {
        ta.addCommand(input);
    }

    /**
     * Adds a given line of output to the console panel
     *
     * @param message line to add
     */
    @Contract(mutates = "this")
    void addMessage(String message) {
        ta.addOutput(message);
    }
}
