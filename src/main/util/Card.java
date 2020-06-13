package util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

// Represents a singular card
public final class Card implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final Font font = new Font("Arial", Font.PLAIN, 20);
    public static final int CARD_WIDTH = 80;
    public static final int CARD_HEIGHT = 150;

    private Suit suit;
    private int number = -1; // -1 if faceCard
    private Face face = null;
    private boolean isFaceCard = false;

    // Card has to be of format <NUM><SUIT> (e.g. 3C, JD)
    public Card(String card) {
        if (card.length() < 2 || card.length() > 3)
            throw new IllegalArgumentException("Invalid card length: " + card.length());
        if (card.length() == 3) {
            suit = Suit.getSuit(card.substring(2));
            if (!card.substring(0, 2).equals("10")) throw new IllegalArgumentException("Invalid card number.");
            number = 10;
        } else {
            suit = Suit.getSuit(card.substring(1));
            if (Character.isDigit(card.charAt(0))) {
                if (Character.digit(card.charAt(0), 10) < 2) throw new IllegalArgumentException("Invalid Card number.");
                else number = Character.digit(card.charAt(0), 10);
            } else {
                isFaceCard = true;
                face = Face.getFace(card.charAt(0));
            }
        }
        assert (isValid());
    }

    // EFFECTS: returns the suit of the card
    public Suit getSuit() {
        return suit;
    }

    // EFFECTS: returns whether the card is a face card
    public boolean isFaceCard() {
        return isFaceCard;
    }

    // EFFECTS: gets the number of the card
    public int getNumber() {
        return number;
    }

    // EFFECTS: returns the face of the card
    public Face getFace() {
        return face;
    }

    // MODIFIES: g
    // EFFECTS: draws the card offset by given x and y coordinates onto graphics object
    public void draw(Graphics g, float x, float y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform af = g2d.getTransform();
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.translate(x, y);
        g2d.drawRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, 20, 20);
        g2d.setFont(font);
        g2d.setColor(getSuit() == Suit.Spade || getSuit() == Suit.Club ? Color.BLACK : Color.RED);
        String firstLine = "" + getSuit().getCharacter();
        String secondLine = isFaceCard() ? getFace().toString() : "" + getNumber();
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(firstLine, 15 - fm.stringWidth(firstLine) / 2, 15);
        g2d.drawString(secondLine, 15 - fm.stringWidth(secondLine) / 2, 35);
        g2d.scale(-1, -1);
        g2d.drawString(firstLine, -CARD_WIDTH + 15 - fm.stringWidth(firstLine) / 2, -CARD_HEIGHT + 15);
        g2d.drawString(secondLine, -CARD_WIDTH + 15 - fm.stringWidth(secondLine) / 2, -CARD_HEIGHT + 35);
        g2d.setTransform(af);
    }

    @Override
    // EFFECTS: returns string representation of this card
    public String toString() {
        if (isFaceCard) {
            return face.toString() + suit.toString();
        } else {
            return number + suit.toString();
        }
    }

    // EFFECTS: determines whether the card is valid
    public final boolean isValid() {
        return suit != null && ((isFaceCard && face != null && number == -1) || (!isFaceCard && face == null && (number < 11 && number > 1)));
    }

    @Override
    // EFFECTS: determines if two cards/objects are equal
    public boolean equals(Object o) {
        if (!(o instanceof Card)) return false;
        Card object = (Card) o;
        if (object.isFaceCard ^ isFaceCard) return false;
        if (!object.suit.equals(suit)) return false;
        if (object.isFaceCard) {
            return object.face.equals(face);
        } else {
            return object.number == number;
        }
    }

    // EFFECTS: determines if this card is a penalty card
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
    public int getPenaltyPoints() {
        if (Suit.Diamond.equals(suit)) return Face.Jack.equals(face) ? -100 : 0;
        if (Suit.Spade.equals(suit)) return Face.Queen.equals(face) ? 100 : 0;
        if (Suit.Club.equals(suit)) return number == 10 ? -50 : 0;
        else {
            if (!isFaceCard && number < 5) return 0;
            else if (!isFaceCard) return 10;
            else return (face.getValue() - 9) * 10;
        }
    }

    @Override
    // EFFECTS: returns the hash code of this card
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + suit.hashCode();
        hash = 97 * hash + this.number;
        hash = 97 * hash + (face == null ? 0 : face.hashCode());
        hash = 97 * hash + (this.isFaceCard ? 1 : 0);
        return hash;
    }

    // EFFECTS: gets the integer value of the card (i.e. 2-10 if number, 11 if J, 12 if Q, etc.)
    public int getValue() {
        if (number != -1) return number;
        else return face.getValue();
    }

    // EFFECTS: determines if this card is the 10 of clubs
    public boolean is10C() {
        return Suit.Club.equals(suit) && number == 10;
    }

    // EFFECTS: determines if this card is the three of clubs
    public boolean is3C() {
        return Suit.Club.equals(suit) && number == 3;
    }

    // EFFECTS: determines if this card is a heart
    public boolean isHeart() {
        return Suit.Heart.equals(suit);
    }

    // EFFECTS: creates a new copy of this card
    public Card copy() {
        return new Card(toString());
    }

    // EFFECTS: determines if the face of this card is the same as the face of the other card
    public boolean equalFace(Card b) {
        if (face == null) return b.face == null;
        return face.equals(b.face);
    }

    // EFFECTS: determines if the suit of this card is the same of the suit of the other card
    public boolean equalSuit(Card b) {
        return suit.equals(b.suit);
    }

    // EFFECTS: determines if the number of this card is the same as the number of the other card
    public boolean equalNumber(Card b) {
        return number == b.getNumber();
    }
}
