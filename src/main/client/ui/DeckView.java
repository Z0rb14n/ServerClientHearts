package client.ui;

import client.ClientGameState;
import client.GameClient;
import org.jetbrains.annotations.Contract;

import javax.swing.*;

/**
 * Represents the view of the deck, containing a CardView, SuitOrderView and a play button
 */
class DeckView extends JPanel {
    private final PlayButton pb = new PlayButton();
    final CardView cv = new CardView(this);
    private final SuitOrderView sov = new SuitOrderView(this);
    private final ClientGameState clientGameState = GameClient.getInstance().getClientState();

    /**
     * @return true if you should pass 3 cards
     */
    boolean isOnThreeCardState() {
        return !clientGameState.getPlayersThatPassed()[clientGameState.getPlayerNumber() - 1] &&
                clientGameState.isStartedPassingCards() && !clientGameState.isAllCardsPassed();
    }

    /**
     * @return true if you should play 1 card
     */
    boolean isOnOneCardState() {
        return clientGameState.canPlay();
    }

    /**
     * Initializes DeckView and its components (i.e. SuitOrderView, play button and card view)
     */
    DeckView() {
        super();
        add(pb);
        sov.initialize();
        cv.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        add(cv);
        sov.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
        add(sov);
    }

    /**
     * Updates components
     */
    @Contract(mutates = "this")
    void update() {
        cv.repaint();
        sov.update();
    }

    /**
     * Updates the play button
     *
     * @implNote Not required to be run every frame, so set in different method
     */
    @Contract(mutates = "this")
    void updatePlayButton() {
        pb.update();
    }

    /**
     * JButton that when pressed plays cards
     */
    private class PlayButton extends JButton {
        /**
         * Initializes the JButton with text and an action listener
         */
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
                setEnabled(false);
            });
        }

        /**
         * Updates enabled state based on number of cards and current state
         */
        void update() {
            if (isOnThreeCardState())
                setEnabled(cv.getNumberSelectedCards() == 3);
            else if (isOnOneCardState()) {
                if (cv.getNumberSelectedCards() == 1 && GameClient.getInstance().getClientState().isValidCardPlay(cv.getActiveCards().get(0)))
                    setEnabled(true);
            } else setEnabled(false);
        }
    }
}
