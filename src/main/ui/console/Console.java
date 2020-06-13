package ui.console;

import javax.swing.*;
import java.awt.*;

// Represents the console window frame
public class Console extends JFrame {
    private static final Dimension SIZE = new Dimension(600, 600);
    private ConsolePanel consolePanel;
    private static Console singleton;

    // EFFECTS: initializes the console window frame with given title
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

    // MODIFIES: Console
    // EFFECTS: ensures there only exists one console instance, while maintaining global access to that instance
    //          see Singleton Design Pattern, Gang of Four
    public static Console getConsole() {
        if (singleton == null) {
            singleton = new Console();
        }
        return singleton;
    }

    // MODIFIES: this
    // EFFECTS: parses and runs the inputted command
    void runCommand(String input) {
        addCommand(input);
        try {
            Command command = new Command(input);
            command.runCommand();
        } catch (InvalidCommandException ignored) {
        }
    }

    // MODIFIES: this
    // EFFECTS: adds the given command to the console panel
    private void addCommand(String input) {
        consolePanel.addCommand(input);
    }

    // MODIFIES: this
    // EFFECTS: adds a given message to console output
    public void addMessage(String output) {
        consolePanel.addMessage(output);
    }
}
