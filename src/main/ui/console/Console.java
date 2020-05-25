package ui.console;

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

    void addCommand(String input) {
        consolePanel.addCommand(input);
    }
}
