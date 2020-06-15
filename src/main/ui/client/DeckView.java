package ui.client;

import javax.swing.*;

// Represents the view of the Deck
class DeckView extends JPanel {
    private CardView cv = new CardView();
    private SuitOrderView sov = new SuitOrderView();

    // EFFECTS: initializes DeckView and its components
    DeckView() {
        super();
        sov.initialize();
        cv.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        add(cv);
        sov.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
        add(sov);
    }

    // MODIFIES: this
    // EFFECTS: updates components
    void update() {
        cv.update();
        sov.update();
    }
}
