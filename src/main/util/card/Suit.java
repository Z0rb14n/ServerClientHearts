package util.card;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static net.Constants.*;

/**
 * Suit of a card
 */
public enum Suit {
    Club(CLUB_UNICODE, "C"),
    Heart(HEART_UNICODE, "H"),
    Diamond(DIAMOND_UNICODE, "D"),
    Spade(SPADE_UNICODE, "S");

    /**
     * Display representation (i.e. if displayed on a string, what unicode character should be displayed)
     */
    private final char displayRep;
    /**
     * Internal string representation
     */
    @NotNull
    private final String internalRep;

    // EFFECTS: initializes the suit with the given representation to be displayed on the card and the internal representation

    /**
     * Initializes suit with given display representation and internal representation
     *
     * @param displayRep  single character display representation
     * @param internalRep internal string representation
     */
    Suit(char displayRep, @NotNull String internalRep) {
        this.displayRep = displayRep;
        this.internalRep = internalRep;
    }

    /**
     * @return Suit color
     */
    @Contract(pure = true)
    public Color getColor() {
        if (this == Club || this == Spade) return Color.BLACK;
        else return Color.RED;
    }

    /**
     * Return suit whose internal representation is given
     *
     * @param internalRep internal representation of suit
     * @return Suit with given internal representation
     * @throws IllegalArgumentException if suit is invalid/not found
     */
    public static Suit getSuit(String internalRep) {
        for (Suit s : Suit.values()) {
            if (s.internalRep.equals(internalRep)) return s;
        }
        throw new IllegalArgumentException("Invalid internal representation of suit: " + internalRep);
    }

    @Override
    @Contract(pure = true)
    @NotNull
    public String toString() {
        return internalRep;
    }

    /**
     * @return displayed character on card
     */
    @Contract(pure = true)
    public char getCharacter() {
        return displayRep;
    }
}
