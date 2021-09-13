package client.ui;

import client.ClientGameState;
import client.GameClient;

import javax.swing.*;

// Represents the view of the Deck
class DeckView extends JPanel {
    private final PlayButton pb = new PlayButton();
    private final CardView cv = new CardView(this);
    private final SuitOrderView sov = new SuitOrderView();
    private final ClientGameState clientGameState = GameClient.getInstance().getClientState();

    boolean isOnThreeCardState() {
        return !clientGameState.getPlayersThatPassed()[clientGameState.getPlayerNumber() - 1] &&
                clientGameState.isStartedPassingCards();
    }

    boolean isOnOneCardState() {
        return clientGameState.canPlay();
    }

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
    private class PlayButton extends JButton {
        // EFFECTS: initializes the text and the mouse listener
        PlayButton() {
            super("Play");
            setEnabled(false);
            addActionListener(e -> {
                if (cv.getActiveCards().size() == 3) {
                    GameClient.getInstance().passCards(cv.getActiveCards().get(0), cv.getActiveCards().get(1), cv.getActiveCards().get(2));
                    cv.setAllCardsInactive();
                } else if (cv.getActiveCards().size() == 1) {
                    GameClient.getInstance().playCard(cv.getActiveCards().get(0));
                    cv.setAllCardsInactive();
                }
            });
        }

        void update() {
            if (isOnThreeCardState())
                setEnabled(cv.getNumberSelectedCards() == 3);
            else if (isOnOneCardState())
                setEnabled(cv.getNumberSelectedCards() == 1);
            else setEnabled(false);
        }
    }
}
