package util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import util.card.Card;
import util.card.Suit;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static util.card.Suit.*;

/**
 * Represents an ordering of cards that can sort a Deck object
 */
public class SuitOrder implements Comparator<Card>, Serializable {
    private static final long serialVersionUID = 1L;

    private boolean reverse = false;
    private boolean sortByValue = false;
    public static final Suit[] DEFAULT = new Suit[]{Heart, Diamond, Spade, Club};
    //{top,secondTop,secondBottom,bottom};
    private Suit[] suits = new Suit[4];

    /**
     * Initializes a default suit order.
     * <p>
     * The default is Hearts > Diamonds > Spades > Clubs
     */
    public SuitOrder() {
        reset();
    }

    /**
     * @return deep copy of this suit order
     */
    @NotNull
    @Contract(pure = true)
    public SuitOrder copy() {
        SuitOrder so = new SuitOrder();
        so.sortByValue = this.sortByValue;
        Suit[] suitArray = new Suit[4];
        System.arraycopy(suits, 0, suitArray, 0, 4);
        so.suits = suitArray;
        so.reverse = reverse;
        return so;
    }

    /**
     * @return true if SuitOrder results should be reversed
     */
    @Contract(pure = true)
    public boolean isReversed() {
        return reverse;
    }

    /**
     * @return true if this suit order is the default suit order
     */
    @Contract(pure = true)
    public boolean isDefault() {
        if (reverse) return false;
        if (sortByValue) return false;
        for (int i = 0; i < 4; i++) {
            if (DEFAULT[i] != suits[i]) return false;
        }
        return true;
    }

    /**
     * @return true if suit order is sorting by value instead of suit
     */
    @Contract(pure = true)
    public boolean isSortingByValue() {
        return sortByValue;
    }

    /**
     * Sets if the suit order should be reversed
     *
     * @param value new suit order reversal state
     */
    @Contract(mutates = "this")
    public void setReverse(boolean value) {
        reverse = value;
    }

    /**
     * Sets if the suit order should be sorting by value
     *
     * @param val new suit order value sorting state
     */
    @Contract(mutates = "this")
    public void setSortByValue(boolean val) {
        sortByValue = val;
    }

    /**
     * Moves a suit to the highest priority and shift everything else down
     *
     * @param suit Suit to shift to the highest priority
     */
    @Contract(mutates = "this")
    public void moveSuitToTop(@NotNull Suit suit) {
        moveSuitToLocation(suit, 1);
    }

    /**
     * @return a copy of the internal ordering of the suits
     */
    @Contract(pure = true)
    public Suit[] getSuitOrder() {
        Suit[] temp = new Suit[4];
        System.arraycopy(suits, 0, temp, 0, 4);
        return temp;
    }

    /**
     * Internally resets this suit order to the default values
     */
    @Contract(mutates = "this")
    public void reset() {
        reverse = false;
        sortByValue = false;
        System.arraycopy(DEFAULT, 0, suits, 0, 4);
    }

    /**
     * Moves a suit to a specific location, shifting suits down/up if needed
     *
     * @param suit     Suit to move to
     * @param location Location to move to (range [1-4], or index+1)
     * @throws IllegalArgumentException if location is out of range [1-4]
     */
    @Contract(mutates = "this")
    public void moveSuitToLocation(@NotNull Suit suit, int location) {
        if (location < 1 || location > 4)
            throw new IllegalArgumentException("Location is out of range [1-4]: " + location);
        final int prevLocation = locateSuit(suit);
        if (prevLocation == location) return;
        if (prevLocation > location) {
            // shifts everything down 1
            // a,b,client,d -> a,a,b,client
            System.arraycopy(suits, location - 1, suits, location, prevLocation - location);
        } else {
            // shifts everything up 1
            System.arraycopy(suits, prevLocation, suits, prevLocation - 1, location - prevLocation);
        }
        suits[location - 1] = suit;
    }

    /**
     * Moves a suit to the bottom, shifting everything else up
     *
     * @param suit Suit to move to the bottom
     */
    @Contract(mutates = "this")
    public void moveSuitToBottom(@NotNull Suit suit) {
        moveSuitToLocation(suit, 4);
    }

    /**
     * Locates the position of the suit in this suit order (top = 1; bottom is 4)
     *
     * @param suit Suit to locate
     * @return position in suit order (from 1-4)
     * @throws IllegalArgumentException if suit cannot be found
     */
    @Contract(pure = true)
    public int locateSuit(Suit suit) {
        if (suits[0].equals(suit)) return 1;
        if (suits[1].equals(suit)) return 2;
        if (suits[2].equals(suit)) return 3;
        if (suits[3].equals(suit)) return 4;
        throw new IllegalArgumentException("Suit not found: " + suit);
    }

    /**
     * Compare two cards strictly by suit
     *
     * @return 1 if a.suit > b.suit; 0 if equal; -1 if a.suit < b.suit
     */
    @Contract(pure = true)
    public int suitCompare(@NotNull Card a, @NotNull Card b) {
        return suitCompare(a.getSuit(), b.getSuit());
    }

    /**
     * Compare two suits by location within the suit order
     *
     * @return 1 if a.suit > b.suit; 0 if equal; -1 otherwise
     * @implNote locateSuit returns smaller numbers with higher priority, so the Integer.compare() call
     * will have arguments reversed
     */
    @Contract(pure = true)
    public int suitCompare(Suit a, Suit b) {
        return Integer.compare(locateSuit(b), locateSuit(a));
    }

    /**
     * Compare two cards strictly by its value
     *
     * @return 1 if a.value > b.value; 0 if equal; -1 if a.value < b.value
     */
    @Contract(pure = true)
    public int valueCompare(@NotNull Card a, @NotNull Card b) {
        return Integer.compare(a.getValue(), b.getValue());
    }

    /**
     * Compare two cards given internal ordering
     *
     * @return 1 if a > b; 0 if a and b are equal; -1 otherwise
     */
    @Override
    @Contract(pure = true)
    public int compare(@NotNull Card a, @NotNull Card b) {
        if (reverse) {
            if (!sortByValue && suitCompare(a, b) != 0) return -suitCompare(a, b);
            else return -valueCompare(a, b);
        } else {
            if (!sortByValue && suitCompare(a, b) != 0) return suitCompare(a, b);
            else return valueCompare(a, b);
        }
    }

    @Override
    @Contract(pure = true)
    public boolean equals(Object o) {
        if (!(o instanceof SuitOrder)) return false;
        SuitOrder so = (SuitOrder) o;
        return Arrays.equals(suits, so.getSuitOrder()) && sortByValue == so.sortByValue && reverse == so.reverse;
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
        return Objects.hash(sortByValue, Arrays.hashCode(suits), reverse);
    }
}
