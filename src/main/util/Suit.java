package util;

import static net.Constants.*;

// Represents a suit
public enum Suit {
    Club(CLUB_UNICODE, "C"),
    Heart(HEART_UNICODE, "H"),
    Diamond(DIAMOND_UNICODE, "D"),
    Spade(SPADE_UNICODE, "S");

    private char rep;
    private String stringRepresentation;

    // EFFECTS: initializes the suit with the given representation to be displayed on the card and the internal representation
    Suit(char representation, String str) {
        rep = representation;
        stringRepresentation = str;
    }

    // EFFECTS: gets the given suit whose internal representation is given
    public static Suit getSuit(String c) {
        for (Suit s : Suit.values()) {
            if (s.stringRepresentation.equals(c)) return s;
        }
        throw new IllegalArgumentException();
    }

    @Override
    // EFFECTS: returns the string representation of the suit (internal representation)
    public String toString() {
        return stringRepresentation;
    }

    // EFFECTS: returns the displayed character on the card
    public char getCharacter() {
        return rep;
    }
}
