package client.ui;

import client.ClientGameState;
import org.jetbrains.annotations.Contract;
import util.card.Deck;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

/**
 * Represents the view of the cards
 */
class CardView extends JPanel implements MouseListener {
    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 150;
    private static final int BORDER_THICKNESS = 2;
    private static final int SIZE_BUFFER = 4 * BORDER_THICKNESS;
    private static final int HEIGHT_OFFSET = 20;
    private final DeckView parent;
    // EFFECTS: initializes CardView
    CardView(DeckView parent) {
        super();
        this.parent = parent;
        setPreferredSize(new Dimension(CARD_WIDTH * 13 + SIZE_BUFFER, CARD_HEIGHT + SIZE_BUFFER + HEIGHT_OFFSET));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, BORDER_THICKNESS));
        addMouseListener(this);
    }

    private int numSelected = 0;
    private final boolean[] activeCards = new boolean[13];

    // EFFECTS: returns the number of selected cards
    int getNumberSelectedCards() {
        return numSelected;
    }

    // EFFECTS: returns the active cards
    Deck getActiveCards() {
        Deck deck = new Deck();
        Deck clientDeck = ClientGameState.getInstance().getPlayerDeck();
        for (int i = 0; i < clientDeck.size(); i++) {
            if (activeCards[i]) deck.add(clientDeck.get(i));
        }
        return deck;
    }

    @Override
    // MODIFIES: g
    // EFFECTS: paints this components onto the given graphics object
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        Deck d = ClientGameState.getInstance().getPlayerDeck();
        for (int i = 0; i < d.size(); i++) {
            int drawnYPos = getY() + BORDER_THICKNESS + HEIGHT_OFFSET;
            if (activeCards[i]) drawnYPos -= HEIGHT_OFFSET;
            DrawUtil.drawCard(g2d, CARD_WIDTH * i + BORDER_THICKNESS, drawnYPos, CARD_WIDTH, CARD_HEIGHT, d.get(i));
        }
        if (!shouldBeActive()) {
            g2d.setColor(new Color(50, 50, 50, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    // MODIFIES: this
    // EFFECTS: toggles the active cards upon mouse click
    public void mouseClicked(MouseEvent e) {
        if (!shouldBeActive()) {
            return;
        }
        ClientGameState state = ClientGameState.getInstance();
        // top left of JPanel is 0,0
        int index = Math.floorDiv(e.getX() - BORDER_THICKNESS, CARD_WIDTH);
        System.out.println("[CardView::mouseClicked]: " + index);
        Deck deck = state.getPlayerDeck();
        if (index >= deck.size() || index < 0) return;
        if (state.shouldPassCards()) {
            if (activeCards[index]) {
                activeCards[index] = false;
                numSelected--;
            } else if (numSelected < 3) {
                activeCards[index] = true;
                numSelected++;
            }
        } else if (parent.isOnOneCardState()) {
            if (activeCards[index]) {
                activeCards[index] = false;
                numSelected = 0;
            } else if (numSelected == 0) {
                if (ClientGameState.getInstance().isCardAllowed(deck.get(index))) {
                    activeCards[index] = true;
                    numSelected = 1;
                }
            }
        }
        parent.updatePlayButton();
        repaint();
    }

    /**
     * Sets all cards to be inactive
     */
    @Contract(mutates = "this")
    void setAllCardsInactive() {
        Arrays.fill(activeCards, false);
        numSelected = 0;
    }

    private boolean shouldBeActive() {
        ClientGameState state = ClientGameState.getInstance();
        if (!state.isAllCardsPassed())
            return state.shouldPassCards();

        return state.canPlay() && state.getPlayerDeck().size() > 0;
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
