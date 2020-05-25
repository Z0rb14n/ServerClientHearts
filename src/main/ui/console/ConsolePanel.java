package ui.console;

import javax.swing.*;

public class ConsolePanel extends JPanel {
    ConsolePanel() {
        super();
        add(new TextArea());
        add(new CommandInput());
    }
}
