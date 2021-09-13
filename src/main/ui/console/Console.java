package ui.console;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ui.console.command.Command;
import ui.console.command.InvalidCommandException;

import javax.swing.*;
import java.awt.*;

/**
 * Represents main console window frame
 */
public class Console extends JFrame {
    private static final Dimension SIZE = new Dimension(600, 600);
    private final ConsolePanel consolePanel;
    private static Console singleton;

    /**
     * Initializes console window frame with given title/size
     */
    private Console() {
        super("Console");
        setResizable(false);
        setPreferredSize(SIZE);
        setSize(SIZE);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        consolePanel = new ConsolePanel();
        add(consolePanel);
        setVisible(true);
    }

    /**
     * Gets the main singleton instance of the console
     *
     * @return main static instance
     */
    @NotNull
    public static Console getConsole() {
        if (singleton == null) {
            singleton = new Console();
        }
        return singleton;
    }

    /**
     * Parses and runs the input command
     *
     * @param input new command input
     */
    @Contract(mutates = "this")
    void runCommand(@NotNull String input) {
        addCommand(input);
        try {
            Command.constructCommand(input).execute();
        } catch (InvalidCommandException ignored) {
        }
    }

    /**
     * Adds given command to console panel
     *
     * @param input string of input command
     */
    @Contract(mutates = "this")
    private void addCommand(@NotNull String input) {
        consolePanel.addCommand(input);
    }

    /**
     * Adds a given message to console output
     *
     * @param output string of output
     */
    @Contract(mutates = "this")
    public void addMessage(String output) {
        System.out.println(output);
        consolePanel.addMessage(output);
    }
}
