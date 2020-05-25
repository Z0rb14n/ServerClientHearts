package ui.console;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class CommandInput extends JPanel {
    private JTextField jtf = new JTextField(30);

    CommandInput() {
        super();
        add(jtf);
        add(new Button());
    }

    private class Button extends JButton {
        Button() {
            super("Submit");
            addMouseListener(new Listener());
        }

        private class Listener extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                Console.getConsole().addCommand(jtf.getText());
                jtf.setText("");
            }
        }
    }
}
