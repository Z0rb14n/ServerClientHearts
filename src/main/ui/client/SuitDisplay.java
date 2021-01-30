package ui.client;

import util.GameClient;
import util.Suit;
import util.SuitOrder;

import javax.swing.*;
import java.awt.*;

// Represents the display of the suits in SuitOrderViews
class SuitDisplay extends JPanel {
    private final JLabel actualSuit;

    // EFFECTS: initializes the suit display with given header JLabel and a suit display
    SuitDisplay() {
        super();
        setLayout(new BorderLayout());
        JLabel header = new JLabel("Bottom ------- Top");
        header.setHorizontalAlignment(JLabel.CENTER);
        add(header, BorderLayout.PAGE_START);
        StringBuilder sb = new StringBuilder();
        Suit[] so = SuitOrder.DEFAULT;
        for (int i = so.length - 1; i >= 0; i--) {
            sb.append(so[i].getCharacter());
            if (i != 0) sb.append(" < ");
        }
        actualSuit = new JLabel(sb.toString());
        actualSuit.setHorizontalAlignment(JLabel.CENTER);
        add(actualSuit, BorderLayout.CENTER);
    }

    // MODIFIES: this
    // EFFECTS: adjusts SuitDisplay according to current MainFrame deck state
    void update() {
        StringBuilder sb = new StringBuilder();
        Suit[] so = GameClient.getInstance().getClientState().getPlayerDeck().getOrder().getSuitOrder();
        for (int i = so.length - 1; i >= 0; i--) {
            sb.append(so[i].getCharacter());
            if (i != 0) sb.append(" < ");
        }
        actualSuit.setText(sb.toString());
        repaint();
    }
}
