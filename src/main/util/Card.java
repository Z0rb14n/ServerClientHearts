package util;

import ui.SCHClient;

import java.io.Serializable;

import static net.Constants.*;
import static util.Face.*;

// Represents a singular card
public final class Card implements Serializable {
    private static final long serialVersionUID = 2L;
    public static final float CARD_WIDTH = 80;
    public static final float CARD_HEIGHT = 150;

    private Suit suit;
    private int number; // -1 if faceCard
    private Face face;
    private boolean isFaceCard;

    // Card has to be of format <NUM><SUIT>
    public Card(String card) {
        if (card.length() < 2 || card.length() > 3)
            throw new IllegalArgumentException("Invalid card length: " + card.length());
        if (card.length() == 3) {
            suit = convertStringToSuit(card.substring(2));
            face = null;
            isFaceCard = false;
            try {
                number = Integer.parseInt(card.substring(0, 2));
                if (number != 10) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid card number: " + e);
            }
        }
        if (card.length() == 2) {
            suit = convertStringToSuit(card.substring(1));
            try {
                number = Integer.parseInt("" + card.charAt(0));
                if (number < 2) throw new IllegalArgumentException("Invalid Card number.");
                face = null;
                isFaceCard = false;
            } catch (NumberFormatException e) {
                number = -1;
                isFaceCard = true;
                if (card.charAt(0) == 'J') {
                    face = Jack;
                } else if (card.charAt(0) == 'A') {
                    face = Ace;
                } else if (card.charAt(0) == 'Q') {
                    face = Queen;
                } else if (card.charAt(0) == 'K') {
                    face = King;
                } else {
                    throw new IllegalArgumentException(card);
                }
            }
        }
        assert (isValid());
    }

    public Suit getSuit() {
        return suit;
    }

    public boolean isFaceCard() {
        return isFaceCard;
    }

    public int getNumber() {
        return number;
    }

    public Face getFace() {
        return face;
    }

    public void draw(float x, float y) {
        SCHClient applet = SCHClient.getClient();
        applet.pushStyle();
        applet.stroke(BLACK);
        applet.strokeWeight(2);
        applet.fill(WHITE);
        applet.textSize(20); // hard coded text size. fite me.
        applet.textLeading(20);
        applet.pushMatrix();
        applet.translate(x, y);
        applet.rect(0, 0, CARD_WIDTH, CARD_HEIGHT, 20);
        int textFillColor = getSuit() == Suit.Spade || getSuit() == Suit.Club ? BLACK : RED;
        String renderedText = "" + getSuit().getCharacter();
        if (isFaceCard()) {
            renderedText += "\n" + getFace().toString();
        } else {
            renderedText += "\n" + getNumber();
        }
        applet.textAlign(applet.CENTER, applet.TOP);
        applet.fill(textFillColor);
        applet.text(renderedText, 15, 10);
        applet.scale(-1, -1);
        applet.text(renderedText, -CARD_WIDTH + 15, -CARD_HEIGHT + 10);
        applet.popMatrix();
        applet.popStyle();
    }

    private static Suit convertStringToSuit(String suit) {
        if (suit.equals("S")) return Suit.Spade;
        if (suit.equals("H")) return Suit.Heart;
        if (suit.equals("C")) return Suit.Club;
        if (suit.equals("D")) return Suit.Diamond;
        throw new IllegalArgumentException("Invalid Suit Character: " + suit);
    }

    @Override
    public final String toString() {
        if (isFaceCard) {
            return face.toString() + suit.toString();
        } else {
            return number + "" + suit.toString();
        }
    }

    final public boolean isValid() {
        return suit != null && ((isFaceCard && face != null && number == -1) || (!isFaceCard && face == null && (number < 11 && number > 1)));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Card)) return false;
        Card object = (Card) o;
        if (object.isFaceCard) {
            if (!isFaceCard) return false;
            return object.suit.equals(suit) && object.face.equals(face);
        } else {
            if (isFaceCard) return false;
            return object.suit.equals(suit) && object.number == number;
        }
    }

    public boolean isPenaltyCard() {
        if (suit.equals(Suit.Heart)) {
            return isFaceCard || number > 4;
        } else if (suit.equals(Suit.Spade)) {
            return Face.Queen.equals(face);
        } else if (suit.equals(Suit.Diamond)) {
            return Face.Jack.equals(face);
        } else if (suit.equals(Suit.Club)) {
            return number == 10;
        }
        throw new IllegalArgumentException("WHAT");
    }

    public int getPenaltyPoints() {
        if (is10C()) return -50;
        if (Suit.Diamond.equals(suit) && Face.Jack.equals(face)) return -100;
        if (Suit.Spade.equals(suit) && Face.Queen.equals(face)) return 100;
        if (Suit.Heart.equals(suit)) {
            if (!isFaceCard && number < 5) return 0;
            else if (!isFaceCard) return 10;
            else return (face.getValue() - 9) * 10;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + suit.hashCode();
        hash = 97 * hash + this.number;
        hash = 97 * hash + face.hashCode();
        hash = 97 * hash + (this.isFaceCard ? 1 : 0);
        return hash;
    }

    public int getValue() {
        if (number != -1) return number;
        else return face.getValue();
    }

    public boolean is10C() {
        return Suit.Club.equals(suit) && number == 10;
    }

    boolean is3C() {
        return Suit.Club.equals(suit) && number == 3;
    }

    public boolean isHeart() {
        return Suit.Heart.equals(suit);
    }

    public Card copy() {
        return new Card(toString());
    }

    public boolean equalFace(Card b) {
        if (face == null) return b.face == null;
        return face.equals(b.face);
    }

    public boolean equalSuit(Card b) {
        return suit.equals(b.suit);
    }

    public boolean equalNumber(Card b) {
        return number == b.getNumber();
    }
}
