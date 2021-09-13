package client.ui;

import client.GameClient;

import javax.swing.*;
import java.awt.event.*;

// TODO SUIT CHANGE FUNCTIONALITY

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

    // EFFECTS: initializes SuitOrderView and its components
    SuitOrderView() {
        super();
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
        boolean isSortingByValue = GameClient.getInstance().getClientState().getPlayerDeck().getOrder().isSortingByValue();
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
        GameClient.getInstance().getClientState().getPlayerDeck().getOrder().setSortByValue(isValue);
        update();
    }


    // MODIFIES: MainFrame
    // EFFECTS: sorts the main application's deck
    private void sort() {
        GameClient.getInstance().getClientState().getPlayerDeck().sort();
        update();
    }

    // MODIFIES: MainFrame
    // EFFECTS: calls the reset function on the SuitOrder in MainFrame
    private void reset() {
        GameClient.getInstance().getClientState().getPlayerDeck().getOrder().reset();
        update();
    }

    // MODIFIES: MainFrame
    // EFFECTS: sets the SuitOrder in MainFrame to reverse or not reverse
    private void setDoReverse(boolean value) {
        GameClient.getInstance().getClientState().getPlayerDeck().getOrder().setReverse(value);
        update();
    }

    // Represents the "Reverse [X]" check box
    private class ReverseCheckBox extends JCheckBox {
        // EFFECTS: initializes the check box with given item listener and title
        ReverseCheckBox() {
            super("Reverse Order");
            addItemListener(new Listener());
        }

        // MODIFIES: this
        // EFFECTS: updates the reversed check box with current "is reversed" state
        void update() {
            setSelected(GameClient.getInstance().getClientState().getPlayerDeck().getOrder().isReversed());
            repaint();
        }

        // Represents the item listener that determines if the reverse button is checked
        private class Listener implements ItemListener {
            @Override
            // MODIFIES: MainFrame
            // EFFECTS: tells the given suit order to update according to the new state of the box
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    setDoReverse(false);
                } else if (e.getStateChange() == ItemEvent.SELECTED) {
                    setDoReverse(true);
                }
            }
        }
    }

    // Represents the Reset button
    private class ResetButton extends JButton {
        // EFFECTS: initializes the reset button and mouse listener
        ResetButton() {
            super("Reset to Default");
            setEnabled(false);
            addMouseListener(new Listener());
        }

        // MODIFIES: this
        // EFFECTS: updates the reset button to the current state of the client state
        void update() {
            setEnabled(!GameClient.getInstance().getClientState().getPlayerDeck().getOrder().isDefault());
        }

        // Represents the Mouse listener that determines if the reset button is clicked
        private class Listener extends MouseAdapter {
            @Override
            // MODIFIES: ResetButton
            // EFFECTS: resets the suit order
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    reset();
                }
            }
        }
    }

    // Represents the Sort button
    private class SortButton extends JButton {
        // EFFECTS: initializes the button with given text and mouse listener
        SortButton() {
            super("Sort");
            setEnabled(false);
            addMouseListener(new Listener());
        }

        // MODIFIES: this
        // EFFECTS: updates the reset button to the current state of the client state
        void update() {
            setEnabled(!GameClient.getInstance().getClientState().getPlayerDeck().isSorted());
        }

        // Represents the Mouse listener that determines if the reset button is clicked
        private class Listener extends MouseAdapter {
            @Override
            // MODIFIES: ResetButton
            // EFFECTS: resets the suit order
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    sort();
                }
            }
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
