package ui.javaver;

import util.ChatMessage;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

class GamePanel extends JPanel {
    private ChatPanel chatPanel;
    private CardView cardView;
    private PlayerView playerView;

    GamePanel() {
        super();
        setLayout(new BorderLayout());
        chatPanel = new ChatPanel();
        add(chatPanel, BorderLayout.LINE_END);
        cardView = new CardView();
        add(cardView, BorderLayout.PAGE_END);
        playerView = new PlayerView();
        add(playerView, BorderLayout.LINE_START);
        setVisible(true);
    }

    void update(LinkedList<ChatMessage> messages) {
        chatPanel.update(messages);
    }
}
