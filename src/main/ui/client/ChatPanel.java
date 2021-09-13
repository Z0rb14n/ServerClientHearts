package ui.client;

import util.ChatMessage;
import util.GameClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// Represents the chat window on the right hand side of the screen
class ChatPanel extends JPanel {
    private final JTextArea chatArea = new JTextArea(10, 20);
    private final JScrollPane jsp = new JScrollPane(chatArea);
    private static final Color CHAT_GREY = new Color(150, 150, 150, 255);
    private static final Color CHAT_ACTIVE = new Color(50, 50, 50, 255);

    // EFFECTS: initializes chat panel with scroll area for text, and the submission area
    ChatPanel() {
        super();
        setBackground(CHAT_GREY);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        setLayout(new BorderLayout());
        chatArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        jsp.setForeground(Color.BLACK);
        jsp.setBackground(CHAT_GREY);
        add(jsp, BorderLayout.CENTER);
        add(new ChatInput(), BorderLayout.PAGE_END);
    }

    // MODIFIES: this
    // EFFECTS: appends the chat message onto the bottom of the chat area
    void appendMessage(ChatMessage c) {
        chatArea.invalidate();
        chatArea.append("\n" + c.toString());
        chatArea.validate();
    }

    // MODIFIES: this
    // EFFECTS: updates the displayed chat message
    void update(List<ChatMessage> messages) {
        LinkedList<ChatMessage> listOfMessages = new LinkedList<ChatMessage>(messages);
        StringBuilder sb = new StringBuilder();
        Iterator<ChatMessage> iterator = listOfMessages.descendingIterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next()).append("\n");
        }
        if (sb.length() == 0) return;
        sb.substring(0, sb.length() - 1); // remove extra \n
        chatArea.setText(sb.toString());
        repaint();
    }

    // MODIFIES: this
    // EFFECTS: scrolls the chat area to the bottom
    void scrollToBottom() {
        jsp.getVerticalScrollBar().setValue(jsp.getVerticalScrollBar().getValue());
    }

    // EFFECTS: represents the chat input area
    private class ChatInput extends JPanel {
        // EFFECTS: initializes the chat input area
        ChatInput() {
            super();
            JTextField jtf = new JTextField(20);
            jtf.setForeground(Color.WHITE);
            jtf.setCaretColor(Color.WHITE);
            jtf.setBackground(CHAT_ACTIVE);
            jtf.setBorder(new EmptyBorder(2, 2, 2, 2));
            jtf.addActionListener(e -> {
                if (jtf.getText().length() != 0) {
                    GameClient.ClientLogger.logMessage("[ChatPanel]: Sending message " + jtf.getText());
                    GameClient.getInstance().sendChatMessage(jtf.getText());
                    jtf.setText("");
                }
            });
            add(jtf);
            JButton button = new JButton("Send");
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        GameClient.ClientLogger.logMessage("[ChatPanel]: Sending message " + jtf.getText());
                        GameClient.getInstance().sendChatMessage(jtf.getText());
                        jtf.setText("");
                    }
                }
            });
            jtf.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    button.setEnabled(jtf.getText().length() != 0);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    button.setEnabled(jtf.getText().length() != 0);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    button.setEnabled(jtf.getText().length() != 0);
                }
            });
            add(button);
        }
    }
}
