package ui.console;

import javax.swing.*;
import java.awt.*;

class TextArea extends JPanel {
    private static final String COMMAND_PREFIX = "> ";
    private JTextArea text = new JTextArea(15, 30);

    TextArea() {
        super();
        text.setEditable(false);
        text.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane jsp = new JScrollPane(text);
        add(jsp);
        setVisible(true);
    }

    void addCommand(String commandInput) {
        text.append(COMMAND_PREFIX + commandInput + "\n");
        repaint();
    }

    void addOutput(String input) {
        text.append(input + "\n");
        repaint();
    }
}
