package client.ui;

import util.GameClient;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

// Represents the view of the Deck
class DeckView extends JPanel {
    private final PlayButton pb = new PlayButton();
    private final CardView cv = new CardView(this);
    private final SuitOrderView sov = new SuitOrderView();

    // EFFECTS: initializes DeckView and its components
    DeckView() {
        super();
        add(pb);
        sov.initialize();
        cv.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        add(cv);
        sov.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
        add(sov);
    }

    // MODIFIES: this
    // EFFECTS: updates components
    void update() {
        cv.repaint();
        sov.update();
    }

    // MODIFIES: this
    // EFFECTS: updates play button (not required to be run every frame)
    void updatePlayButton() {
        pb.update();
    }

    // Represents the play button that plays cards
    private class PlayButton extends JButton implements MouseListener {
        // EFFECTS: initializes the text and the mouse listener
        PlayButton() {
            super("Play");
            setEnabled(false);
            addMouseListener(this);
        }

        void update() {
            if (GameClient.getInstance().getClientState().isAllCardsPassed())
                setEnabled(cv.getNumberSelectedCards() == 1);
            else setEnabled(cv.getNumberSelectedCards() == 3);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (cv.getActiveCards().size() == 3) {
                GameClient.getInstance().passCards(cv.getActiveCards().get(0), cv.getActiveCards().get(1), cv.getActiveCards().get(2));
            }
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
}
