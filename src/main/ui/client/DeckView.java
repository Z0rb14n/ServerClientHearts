package ui.client;

import javax.swing.*;

// Represents the view of the Deck
public class DeckView extends JPanel {
    private CardView cv = new CardView();
    private SuitOrderView sov = new SuitOrderView();

    // EFFECTS: initializes DeckView and its components
    public DeckView() {
        super();
        add(cv);
        add(sov);
    }

    // MODIFIES: this
    // EFFECTS: updates components
    public void update() {
        cv.update();
        sov.update();
    }
}
