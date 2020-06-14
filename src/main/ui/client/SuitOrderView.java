package ui.client;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

// TODO OTHER ITEMS (e.g. reset button, radio buttons for Suit > Value, functionality to change suit)

// Represents the view of the SuitOrder
public class SuitOrderView extends JPanel {
    private ReverseCheckBox rcb = new ReverseCheckBox();

    // EFFECTS: initializes SuitOrderView and its components
    public SuitOrderView() {
        super();
        add(rcb);
    }

    // MODIFIES: this
    // EFFECTS: updates all components
    public void update() {
        rcb.update();
    }

    // MODIFIES: MainFrame
    // EFFECTS: calls the reset function on the SuitOrder in MainFrame
    private void reset() {
        MainFrame.getFrame().getClientState().getDeck().getOrder().reset();
        rcb.update();
    }

    // MODIFIES: MainFrame
    // EFFECTS: sets the SuitOrder in MainFrame to reverse or not reverse
    private void setDoReverse(boolean value) {
        MainFrame.getFrame().getClientState().getDeck().getOrder().setReverse(value);
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
            setSelected(MainFrame.getFrame().getClientState().getDeck().getOrder().isReversed());
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
}
