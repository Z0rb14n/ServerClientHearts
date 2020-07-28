package ui.client;

import util.Card;
import util.Deck;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;

// TODO SELECT CARDS FUNCTIONALITY

// Represents the view of the cards (currently empty)
class CardView extends JPanel implements MouseListener {
    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 150;
    private static final Font font = new Font("Arial", Font.PLAIN, 20);
    private static final int BORDER_THICKNESS = 2;
    private static final int SIZE_BUFFER = 4 * BORDER_THICKNESS;
    private static final int HEIGHT_OFFSET = 20;
    private DeckView parent;
    // EFFECTS: initializes CardView
    CardView(DeckView parent) {
        super();
        this.parent = parent;
        setPreferredSize(new Dimension(CARD_WIDTH * 13 + SIZE_BUFFER, CARD_HEIGHT + SIZE_BUFFER + HEIGHT_OFFSET));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, BORDER_THICKNESS));
        addMouseListener(this);
    }

    private int numSelected = 0;
    private boolean[] activeCards = new boolean[13];

    // EFFECTS: returns the number of selected cards
    int getNumberSelectedCards() {
        return numSelected;
    }

    // EFFECTS: returns the active cards
    Deck getActiveCards() {
        Deck deck = new Deck();
        for (int i = 0; i < MainFrame.getFrame().getClientState().getDeck().deckSize(); i++) {
            if (activeCards[i]) deck.addCard(MainFrame.getFrame().getClientState().getDeck().get(i));
        }
        return deck;
    }

    @Override
    // MODIFIES: g
    // EFFECTS: paints this components onto the given graphics object
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Deck d = MainFrame.getFrame().getClientState().getDeck();
        for (int i = 0; i < d.deckSize(); i++) {
            int drawnYPos = getY() + BORDER_THICKNESS + HEIGHT_OFFSET;
            if (activeCards[i]) drawnYPos -= HEIGHT_OFFSET;
            drawCard(g2d, CARD_WIDTH * i + BORDER_THICKNESS, drawnYPos, d.get(i));
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
        g.setColor(c.getSuit().getColor());
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

    @Override
    // MODIFIES: this
    // EFFECTS: toggles the active cards upon mouse click
    public void mouseClicked(MouseEvent e) {
        System.out.println(e.getX() + "," + e.getY());
        // top left of JPanel is 0,0
        int index = Math.floorDiv(e.getX() - BORDER_THICKNESS, CARD_WIDTH);
        System.out.println(index);
        if (index >= MainFrame.getFrame().getClientState().getDeck().deckSize()) return;
        System.out.println("Toggling...");
        if (!MainFrame.getFrame().getClientState().hasCardsPassed()) {
            if (MainFrame.getFrame().getClientState().getDeck().deckSize() == 13) {
                if (activeCards[index]) {
                    activeCards[index] = false;
                    numSelected--;
                } else if (numSelected < 3) {
                    activeCards[index] = true;
                    numSelected++;
                }
            }
        } else {
            if (activeCards[index]) {
                activeCards[index] = false;
                numSelected = 0;
            } else if (numSelected == 0) {
                activeCards[index] = true;
                numSelected = 1;
            }
        }
        parent.updatePlayButton();
        repaint();
    }

    //<editor-fold desc="Ignore">
    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    //</editor-fold>
}
