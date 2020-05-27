package ui.javaver;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class ConnectionPanel extends JPanel {
    private static final Font font = new Font("Arial", Font.PLAIN, 24);
    private IPEnterBox ipBox;
    private IPEnterButton ipEnterButton;
    private JLabel errorDisplayer = new JLabel("");

    ConnectionPanel() {
        super();
        setBorder(new EmptyBorder(30, 0, 0, 0));
        ipBox = new IPEnterBox();
        ipEnterButton = new IPEnterButton();
    }

    void initialize() {
        setCorrectLayout();
        JLabel jl = new JLabel("Enter IP");
        jl.setAlignmentX(Component.CENTER_ALIGNMENT);
        jl.setFont(font);
        add(jl);
        add(ipBox);
        add(ipEnterButton);
        errorDisplayer.setForeground(Color.RED);
        errorDisplayer.setFont(font);
        errorDisplayer.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(errorDisplayer);
    }

    private void setCorrectLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    void updateErrorDisplayed(String error) {
        errorDisplayer.setText(error);
        repaint();
    }

    String getErrorDisplayed() {
        return errorDisplayer.getText();
    }

    private class IPEnterBox extends JTextField {
        IPEnterBox() {
            super(20);
            setFont(font);
            setAlignmentX(Component.CENTER_ALIGNMENT);
            addActionListener(new onFinalizedInput()); //when they press the enter key
            getDocument().addDocumentListener(new whenInputChanges());
            setMaximumSize(getPreferredSize());
        }

        private class onFinalizedInput implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!ipBox.getText().isEmpty()) {
                    MainFrame.getFrame().attemptLoadClient(ipBox.getText());
                }
            }
        }

        private class whenInputChanges implements DocumentListener {

            @Override
            public void insertUpdate(DocumentEvent e) {
                ipEnterButton.setEnabled(!ipBox.getText().isEmpty());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                ipEnterButton.setEnabled(!ipBox.getText().isEmpty());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                ipEnterButton.setEnabled(!ipBox.getText().isEmpty());
            }
        }
    }

    private class IPEnterButton extends JButton {
        IPEnterButton() {
            super("Connect");
            setFont(font);
            setAlignmentX(Component.CENTER_ALIGNMENT);
            addMouseListener(new Listener());
        }

        private class Listener extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    System.out.print(ipBox.getText());
                    MainFrame.getFrame().attemptLoadClient(ipBox.getText());
                }
            }
        }
    }
}
