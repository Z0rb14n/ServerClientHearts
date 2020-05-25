package ui.console;

import javax.swing.*;
import java.awt.*;

class ConsolePanel extends JPanel {
    private TextArea ta;
    ConsolePanel() {
        super();
        setLayout(new BorderLayout());
        ta = new TextArea();
        add(ta, BorderLayout.CENTER);
        add(new CommandInput(), BorderLayout.PAGE_END);
        setVisible(true);
    }

    void addCommand(String input) {
        ta.addCommand(input);
    }
}
