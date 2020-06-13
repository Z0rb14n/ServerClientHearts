package ui.client;

import javax.swing.*;
import java.awt.*;

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

    void update() {
        chatPanel.update(MainFrame.getFrame().getClientState().getChatMessages());
        cardView.update();
    }
}
