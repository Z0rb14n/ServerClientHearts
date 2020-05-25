package ui.console;

import javax.swing.*;
import java.awt.*;

public class Console extends JFrame {
    private static final Dimension SIZE = new Dimension(600, 600);
    private static Console singleton;

    private Console() {
        super("Console");
        setResizable(false);
        setPreferredSize(SIZE);
        setSize(SIZE);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        add(new ConsolePanel());
        setVisible(true);
    }

    public static Console getConsole() {
        if (singleton == null) {
            singleton = new Console();
        }
        return singleton;
    }
}
