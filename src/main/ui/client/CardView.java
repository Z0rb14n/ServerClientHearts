package ui.client;

import util.Card;
import util.Deck;
import util.Suit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

// TODO SELECT CARDS FUNCTIONALITY

// Represents the view of the cards (currently empty)
class CardView extends JPanel {
    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 150;
    private static final Font font = new Font("Arial", Font.PLAIN, 20);
    private static final int BORDER_THICKNESS = 2;
    private static final int SIZE_BUFFER = 4 * BORDER_THICKNESS;
    // EFFECTS: initializes CardView
    CardView() {
        super();
        setPreferredSize(new Dimension(CARD_WIDTH * 13 + SIZE_BUFFER, CARD_HEIGHT + SIZE_BUFFER));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, BORDER_THICKNESS));
    }

    // MODIFIES: this
    // EFFECTS: updates the display of all cards played
    void update() {
    }

    @Override
    // MODIFIES: g
    // EFFECTS: paints this components onto the given graphics object
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Deck d = MainFrame.getFrame().getClientState().getDeck();
        int i = 0;
        for (Card c : d) {
            drawCard(g2d, CARD_WIDTH * i + BORDER_THICKNESS, getY() + BORDER_THICKNESS, c);
            i++;
        }
    }

    // MODIFIES: g
    // EFFECTS: draws the card offset by given x and y coordinates onto graphics object
    private static void drawCard(Graphics2D g, int x, int y, Card c) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform af = g.getTransform();
        g.setStroke(new BasicStroke(2));
        g.translate(x, y);
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, 20, 20);
        g.setColor(Color.BLACK);
        g.drawRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, 20, 20);
        g.setFont(font);
        g.setColor(c.getSuit() == Suit.Spade || c.getSuit() == Suit.Club ? Color.BLACK : Color.RED);
        String firstLine = "" + c.getSuit().getCharacter();
        String secondLine = c.isFaceCard() ? c.getFace().toString() : "" + c.getNumber();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(firstLine, 15 - fm.stringWidth(firstLine) / 2, 15);
        g.drawString(secondLine, 15 - fm.stringWidth(secondLine) / 2, 35);
        g.scale(-1, -1);
        g.drawString(firstLine, -CARD_WIDTH + 15 - fm.stringWidth(firstLine) / 2, -CARD_HEIGHT + 15);
        g.drawString(secondLine, -CARD_WIDTH + 15 - fm.stringWidth(secondLine) / 2, -CARD_HEIGHT + 35);
        g.setTransform(af);
    }
}
