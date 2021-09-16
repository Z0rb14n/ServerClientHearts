package client.ui;

import client.GameClient;
import util.card.Deck;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

// Represents the view of the SuitOrder
class SuitOrderView extends JPanel {
    private final ReverseCheckBox rcb = new ReverseCheckBox();
    private final ResetButton rb = new ResetButton();
    private final SuitDisplay sd = new SuitDisplay();
    private final SortButton sb = new SortButton();
    private static final String SUIT_STRING = "Suit";
    private static final String VALUE_STRING = "Value";
    private final JRadioButton preferSuit = new JRadioButton(SUIT_STRING, true);
    private final JRadioButton preferValue = new JRadioButton(VALUE_STRING, false);
    private final Deck deck = GameClient.getInstance().getClientState().getPlayerDeck();
    private final DeckView parent;

    // EFFECTS: initializes SuitOrderView and its components
    SuitOrderView(DeckView parent) {
        super();
        this.parent = parent;
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(preferSuit);
        buttonGroup.add(preferValue);
    }

    // MODIFIES: this
    // EFFECTS: initializes the SuitOrderView to use proper layouts
    void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        sd.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        add(sd);
        rcb.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        add(rcb);
        preferSuit.addActionListener(new RadioListener());
        preferValue.addActionListener(new RadioListener());
        JPanel radioPanel = new JPanel();
        radioPanel.add(preferSuit);
        radioPanel.add(preferValue);
        radioPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        add(radioPanel);
        sb.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        add(sb);
        rb.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        add(rb);
    }

    // MODIFIES: this
    // EFFECTS: updates all components
    void update() {
        boolean isSortingByValue = deck.getOrder().isSortingByValue();
        preferValue.setSelected(isSortingByValue);
        preferSuit.setSelected(!isSortingByValue);
        rcb.update();
        rb.update();
        sd.update();
        sb.update();
    }

    // MODIFIES: MainFrame
    // EFFECTS: adjusts the state of the main application's deck's sorting order
    private void setSortOrder(boolean isValue) {
        deck.getOrderNonCopy().setSortByValue(isValue);
        update();
    }

    // MODIFIES: MainFrame
    // EFFECTS: calls the reset function on the SuitOrder in MainFrame
    private void reset() {
        deck.getOrderNonCopy().reset();
        update();
    }

    // MODIFIES: MainFrame
    // EFFECTS: sets the SuitOrder in MainFrame to reverse or not reverse
    private void setDoReverse(boolean value) {
        deck.getOrderNonCopy().setReverse(value);
        update();
    }

    // Represents the "Reverse [X]" check box
    private class ReverseCheckBox extends JCheckBox {
        /**
         * Initializes the JCheckBox with given item listener and title
         */
        ReverseCheckBox() {
            super("Reverse Order");
            addItemListener(e -> setDoReverse(e.getStateChange() == ItemEvent.SELECTED));
        }

        // MODIFIES: this
        // EFFECTS: updates the reversed check box with current "is reversed" state
        void update() {
            setSelected(deck.getOrder().isReversed());
            repaint();
        }
    }

    // Represents the Reset button
    private class ResetButton extends JButton {
        // EFFECTS: initializes the reset button and mouse listener
        ResetButton() {
            super("Reset to Default");
            setEnabled(false);
            addActionListener(e -> reset());
        }

        // MODIFIES: this
        // EFFECTS: updates the reset button to the current state of the client state
        void update() {
            setEnabled(!deck.getOrder().isDefault());
        }
    }

    // Represents the Sort button
    private class SortButton extends JButton {
        // EFFECTS: initializes the button with given text and mouse listener
        SortButton() {
            super("Sort");
            setEnabled(false);
            addActionListener(e -> {
                deck.sort();
                parent.cv.setAllCardsInactive();
                ClientFrame.getFrame().update();
            });
        }

        // MODIFIES: this
        // EFFECTS: updates the reset button to the current state of the client state
        void update() {
            setEnabled(!deck.isSorted());
        }
    }

    // Represents the item listener for the Radio buttons
    private class RadioListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(VALUE_STRING)) {
                setSortOrder(true);
            } else if (e.getActionCommand().equals(SUIT_STRING)) {
                setSortOrder(false);
            }
        }
    }
}
