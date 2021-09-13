package util.card;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a singular card with a suit, a number (if not a face card), or a face (if a face card).
 * Note that the number will be -1 if it is a face card.
 * <p>
 * Number is not to be confused with value, which is the approximate value of the number and/or face card.
 */
public final class Card implements Serializable {
    private static final long serialVersionUID = 2L;

    @NotNull
    private final Suit suit;
    private final int number; // -1 if faceCard
    @Nullable
    private final Face face;
    private final boolean isFaceCard;

    /**
     * Creates a non-face card with given suit and number
     *
     * @param suit   Suit of the card
     * @param number number of the card
     * @throws IllegalArgumentException if number is out of range [2-10]
     */
    public Card(@NotNull Suit suit, int number) {
        if (number < 2 || number > 10) throw new IllegalArgumentException("Number out of range [2-10]: " + number);
        this.suit = suit;
        this.number = number;
        face = null;
        isFaceCard = false;
    }

    /**
     * Creates a face card with given suit and face
     *
     * @param suit Suit of new card
     * @param face Face of new card
     */
    public Card(@NotNull Suit suit, @NotNull Face face) {
        this.suit = suit;
        this.number = -1;
        this.face = face;
        this.isFaceCard = true;
    }

    /**
     * Creates a card with given format -NUM--SUIT- (e.g. 3C, JD)
     *
     * @param card String representation of card
     * @throws IllegalArgumentException if length of string is invalid, card number of string is incorrect
     */
    public Card(String card) {
        if (card.length() < 2 || card.length() > 3)
            throw new IllegalArgumentException("Invalid card length: " + card.length());
        if (card.length() == 3) {
            suit = Suit.getSuit(card.substring(2));
            if (!card.startsWith("10")) throw new IllegalArgumentException("Invalid card number.");
            number = 10;
            face = null;
            isFaceCard = false;
        } else {
            suit = Suit.getSuit(card.substring(1));
            if (Character.isDigit(card.charAt(0))) {
                if (Character.digit(card.charAt(0), 10) < 2) throw new IllegalArgumentException("Invalid Card number.");
                else number = Character.digit(card.charAt(0), 10);
                face = null;
                isFaceCard = false;
            } else {
                isFaceCard = true;
                face = Face.getFace(card.charAt(0));
                number = -1;
            }
        }
        assert (isValid());
    }

    /**
     * Gets the suit of the card
     */
    @Contract(pure = true)
    public @NotNull Suit getSuit() {
        return suit;
    }

    /**
     * Determines if the card is a face card
     *
     * @return whether the card is a face card
     */
    @Contract(pure = true)
    public boolean isFaceCard() {
        return isFaceCard;
    }

    /**
     * Gets the number of the card, -1 if a face card
     *
     * @return Number of the card
     */
    @Contract(pure = true)
    public int getNumber() {
        return number;
    }

    /**
     * Gets the face of the card, null if not a face card
     *
     * @return Face displayed on the card
     */
    @Contract(pure = true)
    public @Nullable Face getFace() {
        return face;
    }

    @Override
    @Contract(pure = true)
    public String toString() {
        if (isFaceCard) {
            assert (face != null);
            return face.toString() + suit;
        } else {
            return number + suit.toString();
        }
    }

    // EFFECTS: determines whether the card is valid
    @Contract(pure = true)
    public boolean isValid() {
        return isFaceCard && face != null && number == -1 || !isFaceCard && face == null && number < 11 && number > 1;
    }

    @Override
    // EFFECTS: determines if two cards/objects are equal
    @Contract(pure = true)
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof Card)) return false;
        Card object = (Card) o;
        if (object.isFaceCard ^ isFaceCard) return false;
        if (!object.suit.equals(suit)) return false;
        if (object.isFaceCard) {
            return Objects.equals(object.face, face);
        } else {
            return object.number == number;
        }
    }

    // EFFECTS: determines if this card is a penalty card
    @Contract(pure = true)
    public boolean isPenaltyCard() {
        if (suit.equals(Suit.Heart)) {
            return isFaceCard || number > 4;
        } else if (suit.equals(Suit.Spade)) {
            return Face.Queen.equals(face);
        } else if (suit.equals(Suit.Diamond)) {
            return Face.Jack.equals(face);
        } else {
            return number == 10;
        }
    }

    // EFFECTS: determines the amount of penalty points this card entails, or 0
    @Contract(pure = true)
    public int getPenaltyPoints() {
        if (Suit.Diamond.equals(suit)) return Face.Jack.equals(face) ? -100 : 0;
        if (Suit.Spade.equals(suit)) return Face.Queen.equals(face) ? 100 : 0;
        if (Suit.Club.equals(suit)) return number == 10 ? -50 : 0;
        else {
            if (!isFaceCard && number < 5) return 0;
            else if (!isFaceCard) return 10;
            assert (face != null);
            return (face.getValue() - 9) * 10;
        }
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
        return Objects.hash(suit, number, face, isFaceCard);
    }

    /**
     * Gets the integer value of the card (i.e. 2-10 if number, 11 if J, 12 if Q, etc.)
     *
     * @return integer value of this card
     */
    @Contract(pure = true)
    public int getValue() {
        if (number != -1) return number;
        assert (face != null);
        return face.getValue();
    }

    /**
     * Determines if this card is the 10 of clubs
     *
     * @return true if this card is the 10 of clubs
     */
    @Contract(pure = true)
    public boolean is10C() {
        return Suit.Club.equals(suit) && number == 10;
    }

    /**
     * Determines if this card is the three of clubs
     *
     * @return whether this card is the three of clubs
     */
    @Contract(pure = true)
    public boolean is3C() {
        return Suit.Club.equals(suit) && number == 3;
    }


    /**
     * Determines if this card is a heart
     *
     * @return Whether this card is a heart
     */
    @Contract(pure = true)
    public boolean isHeart() {
        return Suit.Heart.equals(suit);
    }

    /**
     * Creates a new copy of this card
     *
     * @return New copy of this card
     */
    @Contract(pure = true)
    @NotNull
    public Card copy() {
        return new Card(toString());
    }

    /**
     * Determines if the face on this card is the same as the face on the other card
     *
     * @param b Other card to compare face to
     * @return true if the face on this card is the same as the other card (or null)
     */
    @Contract(pure = true)
    public boolean equalFace(@NotNull Card b) {
        if (face == null) return b.face == null;
        return face.equals(b.face);
    }

    /**
     * Determines if the suit of this card is the same as the suit of the other card
     *
     * @param b Other card to compare suit to
     * @return true if suits are the same
     */
    @Contract(pure = true)
    public boolean equalSuit(@NotNull Card b) {
        return suit.equals(b.suit);
    }

    /**
     * Determines if the number of this card is the same as the number of the other card
     *
     * @param b Other card to compare number to
     * @return true if the number on this card is the same as the number on the other card
     */
    @Contract(pure = true)
    public boolean equalNumber(@NotNull Card b) {
        return number == b.getNumber();
    }
}
