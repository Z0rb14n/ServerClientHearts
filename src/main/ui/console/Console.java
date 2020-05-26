package ui.console;

import exception.InvalidCommandException;

import javax.swing.*;
import java.awt.*;

public class Console extends JFrame {
    private static final Dimension SIZE = new Dimension(600, 600);
    private ConsolePanel consolePanel;
    private static Console singleton;

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

    public static Console getConsole() {
        if (singleton == null) {
            singleton = new Console();
        }
        return singleton;
    }

    void runCommand(String input) {
        addCommand(input);
        try {
            Command command = new Command(input);
            command.runCommand();
        } catch (InvalidCommandException ignored) {
        }
    }

    private void addCommand(String input) {
        consolePanel.addCommand(input);
    }

    public void addMessage(String output) {
        consolePanel.addMessage(output);
    }
}
