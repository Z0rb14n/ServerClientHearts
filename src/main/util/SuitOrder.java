package util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import static util.Suit.*;

// Represents an ordering of cards
public class SuitOrder implements Comparator<Card>, Serializable {
    private static final long serialVersionUID = 1L;

    private boolean sortByValue;
    public static final Suit[] DEFAULT = new Suit[]{Heart, Diamond, Spade, Club};
    //{top,secondTop,secondBottom,bottom};
    private Suit[] suits = new Suit[4];

    public SuitOrder() {
        // Defaults: Hearts > Diamonds > Spades > Clubs
        reset();
    }

    SuitOrder copy() {
        SuitOrder so = new SuitOrder();
        so.sortByValue = this.sortByValue;
        Suit[] suitArray = new Suit[4];
        System.arraycopy(suits, 0, suitArray, 0, 4);
        so.suits = suitArray;
        return so;
    }

    public boolean isSortingByValue() {
        return sortByValue;
    }

    public void setSortByValue(boolean val) {
        sortByValue = val;
    }

    // MODIFIES: this
    // EFFECTS: moves a suit to top, shifts everything else down
    public void moveSuitToTop(Suit a) {
        moveSuitToLocation(a, 1);
    }

    // EFFECTS: returns the order of the suits
    public Suit[] getSuitOrder() {
        Suit[] temp = new Suit[4];
        System.arraycopy(suits, 0, temp, 0, 4);
        return temp;
    }

    // MODIFIES: this
    // EFFECTS: resets SuitOrder
    public void reset() {
        sortByValue = false;
        System.arraycopy(DEFAULT, 0, suits, 0, 4);
    }

    // MODIFIES: this
    // EFFECTS: moves a suit to specific location (1-4, or index + 1)
    public void moveSuitToLocation(Suit a, int location) {
        final int prevLocation = locateSuit(a);
        if (prevLocation == location) return;
        if (prevLocation > location) {
            // shifts everything down 1
            // a,b,client,d -> a,a,b,client
            System.arraycopy(suits, location - 1, suits, location, prevLocation - location);
        } else {
            // shifts everything up 1
            System.arraycopy(suits, prevLocation, suits, prevLocation - 1, location - prevLocation);
        }
        suits[location - 1] = a;
    }

    // MODIFIES: this
    // EFFECTS: moves suit a to bottom, shifts everything else up
    public void moveSuitToBottom(Suit a) {
        moveSuitToLocation(a, 4);
    }

    // EFFECTS: returns position from top (e.g. top is 1, bottom is 4)
    public int locateSuit(Suit a) {
        if (suits[0].equals(a)) return 1;
        if (suits[1].equals(a)) return 2;
        if (suits[2].equals(a)) return 3;
        if (suits[3].equals(a)) return 4;
        throw new IllegalArgumentException();
    }

    // EFFECTS: returns 1 if a.suit > b.suit, 0 if equal, -1 if a.suit < b.suit
    public int suitCompare(Card a, Card b) {
        return suitCompare(a.getSuit(),b.getSuit());
    }

    // EFFECTS: returns 1 if a > b, 0 if equal, -1 if a < b
    public int suitCompare(Suit a, Suit b) {
        return Integer.compare(locateSuit(b), locateSuit(a));
        // order is reversed (locateSuit(b),locateSuit(a)) because locateSuit returns small numbers if top, large if bottom
    }

    // EFFECTS: returns 1 if a.value > b.value, 0 if equal, -1 if a.value < b.value
    public int valueCompare(Card a, Card b) {
        return Integer.compare(a.getValue(), b.getValue());
    }

    @Override
    // EFFECTS: returns 1 if a > b, 0 if a == b, -1 if a < b
    public int compare(Card a, Card b) {
        if (!sortByValue && suitCompare(a, b) != 0) return suitCompare(a, b);
        else return valueCompare(a, b);
    }

    @Override
    // EFFECTS: returns true if the two objects are equal
    public boolean equals(Object o) {
        if (!(o instanceof SuitOrder)) return false;
        SuitOrder so = (SuitOrder) o;
        return Arrays.equals(suits, so.getSuitOrder()) && sortByValue == so.isSortingByValue();
    }

    @Override
    // EFFECTS: returns hash code of this SuitOrder
    public int hashCode() {
        int result = 17;
        result = 31 * result + (sortByValue ? 0 : 1);
        result = 31 * result + Arrays.hashCode(suits);
        return result;
    }
}
