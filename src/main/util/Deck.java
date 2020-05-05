package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

// Represents a deck of cards
public class Deck implements Iterable<Card>, Serializable {
    private static final long serialVersionUID = 1L;

    private SuitOrder order;
    private final ArrayList<Card> cards;

    // EFFECTS: Initializes the deck of cards with default suit order
    public Deck() {
        order = new SuitOrder();
        cards = new ArrayList<>(13);
    }

    // EFFECTS: gets the suit order of the deck
    public SuitOrder getOrder() {
        return order;
    }

    // MODIFIES: this
    // EFFECTS: sets the suit order to given suit order
    public void setOrder(SuitOrder so) {
        order = so;
    }

    // EFFECTS: determines whether deck is sorted
    public boolean isSorted() {
        for (int i = cards.size() - 1; i > 0; i--) {
            if (order.compare(cards.get(i), cards.get(i - 1)) < 0) return false;
        }
        return true;
    }

    // MODIFIES: this
    // EFFECTS: clears the deck
    public void clear() {
        cards.clear();
    }

    // MODIFIES: this
    // EFFECTS: sorts the deck with internal suit order
    public void sort() {
        cards.sort(order);
    }

    // EFFECTS: returns the number of cards in the deck
    public int deckSize() {
        return cards.size();
    }

    // EFFECTS: returns whether the deck is empty or not
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    // MODIFIES: this
    // EFFECTS: adds a card to the deck
    public void addCard(Card a) {
        cards.add(a);
    }

    // MODIFIES: this
    // EFFECTS: adds the contents of the other deck to this deck
    public void addAll(Deck a) {
        cards.addAll(a.cards);
    }

    // MODIFIES: this
    // EFFECTS; removes a card from the deck
    public void removeCard(Card a) {
        cards.remove(a);
    }

    // MODIFIES: this
    // EFFECTS: removes all cards that does not match the given suit
    public void removeNonMatchingSuit(Suit a) {
        cards.removeIf(t -> !t.getSuit().equals(a));
    }

    // MODIFIES: this
    // EFFECTS: removes non-penalty cards
    public void removeNonPenaltyCards() {
        cards.removeIf(t -> !t.isPenaltyCard());
    }

    // EFFECTS: checks whether the deck contains the three of clubs (cuz lazy);
    public boolean containsThreeOfClubs() {
        return cards.contains(new Card("3C"));
    }

    // EFFECTS: checks whether the deck contains the 10 of clubs (cuz lazy);
    public boolean containsTenOfClubs() {
        return cards.contains(new Card("10C"));
    }

    // EFFECTS: checks whether the deck contains a card of given suit
    public boolean containsSuit(Suit a) {
        for(Card c : cards) {
            if (c.getSuit().equals(a)) return true;
        }
        return false;
    }

    // EFFECTS: checks whether the deck contains the card
    public boolean contains(Card a) {
        return cards.contains(a);
    }

    // REQUIRES: cards.size() == 52 or generate52() has to be called prior
    // MODIFIES: d1, d2, d3, d4, this
    // EFFECTS: distributes all 52 cards in this given deck into d1,d2, d3 and d4,
    public void randomlyDistribute(Deck d1, Deck d2, Deck d3, Deck d4) {
        assert(cards.size() == 52);
        Random random = new Random();
        for(int i = 13; i > 0; i--) {
            int index = random.nextInt(i*4);
            Card card = cards.get(index);
            cards.remove(index);
            d1.addCard(card);
            index = random.nextInt(i*4-1);
            card = cards.get(index);
            cards.remove(index);
            d2.addCard(card);
            index = random.nextInt(i*4-2);
            card = cards.get(index);
            cards.remove(index);
            d3.addCard(card);
            index = random.nextInt(i*4-3);
            card = cards.get(index);
            cards.remove(index);
            d4.addCard(card);
        }
        assert(d1.deckSize() == 13);
        assert(d2.deckSize() == 13);
        assert(d3.deckSize() == 13);
        assert(d4.deckSize() == 13);
        assert(cards.isEmpty());
    }

    // EFFECTS: returns a SHALLOW COPY of all playable cards in the deck
    public Deck getPlayableCards(Suit a) {
        if (!containsSuit(a)) return copy();
        Deck play = new Deck();
        for(Card c : cards) {
            if (c.getSuit().equals(a)) play.addCard(c);
        }
        return play;
    }

    // EFFECTS: returns the number of penalty cards in the deck
    public int numPenaltyCards() {
        int value = 0;
        for(Card c : cards) {
            if (c.isPenaltyCard()) value++;
        }
        return value;
    }

    // EFFECTS: returns the number of penalty points from the deck
    public int penaltyPoints() {
        if (containsTenOfClubs() && numPenaltyCards() == 1) return -50;
        int value = 0;
        if (isShootPossible()) {
            if (containsTenOfClubs()) {
                for(Card c : cards) {
                    if (c.isPenaltyCard() && !c.is10C()) {
                        value += -Math.abs(c.getPenaltyPoints()) * 2;
                    }
                }
            } else {
                for(Card c : cards) {
                    if (c.isPenaltyCard()) {
                        value += -Math.abs(c.getPenaltyPoints());
                    }
                }
            }
        } else {
            if (containsTenOfClubs()) {
                for(Card c : cards) {
                    if (c.isPenaltyCard() && !c.is10C()) {
                        value += c.getPenaltyPoints() * 2;
                    }
                }
            } else {
                for(Card c : cards) {
                    if (c.isPenaltyCard()) value += c.getPenaltyPoints();
                }
            }
        }
        return value;
    }

    // EFFECTS: returns whether a shoot is possible (i.e. you have all hearts from 2H to AH)
    public boolean isShootPossible() {
        boolean[] contains = new boolean[13];
        for (Card c : cards) {
            if (c.isHeart()) {
                contains[c.getValue() - 2] = true;
            }
        }
        for (boolean b : contains) {
            if (!b) return false;
        }
        return true;
    }

    // MODIFIES: this
    // EFFECTS: generates a deck of 52 cards
    public void generate52() {
        assert (cards.isEmpty());
        for (Suit s: Suit.values()) {
            for (int i = 2; i < 11; i++) {
                cards.add(new Card("" + i + s.toString()));
            }
            for (Face f : Face.values()) {
                cards.add(new Card(f.toString() + s.toString()));
            }
        }
        isValid();
    }

    @Override
    // EFFECTS: returns string representation of this deck
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            sb.append(cards.get(i).toString());
            if (i != cards.size() -1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    // EFFECTS: checks whether this deck is valid, but does not check duplicates
    public boolean isValid() {
        for (Card c: cards) {
            if (!c.isValid()) {
                return false;
            }
        }
        return true;
    }

    // EFFECTS: returns index of highest value card of given suit
    public int highestIndexOfSuit(Suit a) {
        int index = -1;
        int value = -1;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getSuit().equals(a)) {
                if (cards.get(i).getValue() > value) {
                    value = cards.get(i).getValue();
                    index = i;
                }
            }
        }
        return index;
    }

    // EFFECTS: returns a copy of the deck
    public Deck copy() {
        Deck deck = new Deck();
        for (Card a : cards) {
            deck.addCard(a);
        }
        return deck;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Deck)) return false;
        Deck deck = (Deck) o;
        return deck.cards.equals(this.cards) && this.order.equals(deck.order);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + order.hashCode();
        result = 31 * result + cards.hashCode();
        return result;
    }

    @Override
    // EFFECTS: returns an iterator over the cards
    public Iterator<Card> iterator() {
        return cards.iterator();
    }
}
