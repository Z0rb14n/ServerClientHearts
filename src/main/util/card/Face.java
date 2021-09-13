package util.card;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the face value of a card
 */
public enum Face {
    Jack(11, "J"),
    Queen(12, "Q"),
    King(13, "K"),
    Ace(14, "A");
    private final int value;
    @NotNull
    private final String rep;

    /**
     * Initializes the face value with given value and face character
     *
     * @param val integer value (e.g. jack is 11)
     * @param rep Character/string representation of this face
     */
    Face(int val, @NotNull String rep) {
        this.value = val;
        this.rep = rep;
    }

    /**
     * Gets the face value corresponding to the given character
     *
     * @param c character in question
     * @return Face value with matching string representation
     * @throws IllegalArgumentException if no such face is found
     */
    public static Face getFace(char c) {
        for (Face f : Face.values()) {
            if (f.toString().charAt(0) == c) return f;
        }
        throw new IllegalArgumentException("Invalid face letter: " + c);
    }

    @Override
    @Contract(pure = true)
    @NotNull
    public String toString() {
        return rep;
    }

    /**
     * @return numeric value of this card (J = 11)
     */
    @Contract(pure = true)
    public int getValue() {
        return value;
    }
}
