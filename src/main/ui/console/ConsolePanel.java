package ui.console;

import javax.swing.*;
import java.awt.*;

// Represents the console panel
class ConsolePanel extends JPanel {
    private TextArea ta;

    // EFFECTS: initializes console panel with correct layout and text area
    ConsolePanel() {
        super();
        setLayout(new BorderLayout());
        ta = new TextArea();
        add(ta, BorderLayout.CENTER);
        add(new CommandInput(), BorderLayout.PAGE_END);
        setVisible(true);
    }

    // MODIFIES: this
    // EFFECTS: adds a command to the console panel
    void addCommand(String input) {
        ta.addCommand(input);
    }

    // MODIFIES: this
    // EFFECTS: adds a given output to the console panel
    void addMessage(String message) {
        ta.addOutput(message);
    }
}
