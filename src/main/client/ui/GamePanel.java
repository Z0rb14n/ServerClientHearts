package client.ui;

import client.GameClient;
import util.ChatMessage;

import javax.swing.*;
import java.awt.*;

class GamePanel extends JPanel {
    private final ChatPanel chatPanel;
    private final DeckView deckView;
    private final PlayerView playerView;

    GamePanel() {
        super();
        setLayout(new BorderLayout());
        chatPanel = new ChatPanel();
        add(chatPanel, BorderLayout.LINE_END);
        deckView = new DeckView();
        add(deckView, BorderLayout.PAGE_END);
        playerView = new PlayerView();
        add(playerView, BorderLayout.LINE_START);
        setVisible(true);
    }

    void addNewChatMessage(ChatMessage c) {
        chatPanel.appendMessage(c);
    }

    void update() {
        chatPanel.update(GameClient.getInstance().getChatMessages());
        playerView.repaint();
        deckView.update();
    }
}
