package util;

// Represents the face value of a card
public enum Face {
    Jack(11, "J"),
    Queen(12, "Q"),
    King(13, "K"),
    Ace(14, "A");
    private int value;
    private String rep;

    Face(int val, String rep) {
        this.value = val;
        this.rep = rep;
    }

    @Override
    public String toString() {
        return rep;
    }

    public int getValue() {
        return value;
    }
}
