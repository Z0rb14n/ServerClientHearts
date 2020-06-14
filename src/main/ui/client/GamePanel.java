package ui.client;

import util.ChatMessage;

import javax.swing.*;
import java.awt.*;

class GamePanel extends JPanel {
    private ChatPanel chatPanel;
    private DeckView deckView;
    private PlayerView playerView;

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

    }

    void update() {
        chatPanel.update(MainFrame.getFrame().getClientState().getChatMessages());
        deckView.update();
    }
}
