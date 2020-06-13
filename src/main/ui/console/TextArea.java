package ui.console;

import javax.swing.*;
import java.awt.*;

// Represents the console text area
class TextArea extends JPanel {
    private static final String COMMAND_PREFIX = "> ";
    private JTextArea text = new JTextArea(15, 30);

    // EFFECTS: initializes the text area with given margins and scroll area
    TextArea() {
        super();
        text.setEditable(false);
        text.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane jsp = new JScrollPane(text);
        add(jsp);
        setVisible(true);
    }

    // MODIFIES: this
    // EFFECTS: adds command to the console window
    void addCommand(String commandInput) {
        text.append(COMMAND_PREFIX + commandInput + "\n");
        repaint();
    }

    // MODIFIES: this
    // EFFECTS: adds output message to console window
    void addOutput(String input) {
        text.append(input + "\n");
        repaint();
    }
}
