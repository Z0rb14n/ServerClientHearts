package util;

import static net.MessageConstants.*;

// Represents a suit
public enum Suit {
    Club(CLUB_UNICODE, "C"),
    Heart(HEART_UNICODE, "H"),
    Diamond(DIAMOND_UNICODE, "D"),
    Spade(SPADE_UNICODE, "S");

    private char rep;
    private String stringRepresentation;

    Suit(char representation, String str) {
        rep = representation;
        stringRepresentation = str;
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }

    public char getCharacter() {
        return rep;
    }


}
