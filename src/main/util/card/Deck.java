package util.card;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import util.SuitOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a deck of cards
 */
public class Deck implements Iterable<Card>, Serializable {
    private static final long serialVersionUID = 69420L;

    @NotNull
    private SuitOrder order = new SuitOrder();
    @NotNull
    private final ArrayList<Card> cards = new ArrayList<>(13);

    /**
     * Initializes deck of cards with default suit order
     */
    public Deck() {
    }

    /**
     * Gets a copy of suit order of the deck
     *
     * @return copy of the suit order of the deck
     */
    @NotNull
    @Contract(pure = true)
    public SuitOrder getOrder() {
        return order.copy();
    }

    /**
     * Gets the suit order of the deck without making a copy
     *
     * @return suit order of the deck
     */
    @NotNull
    public SuitOrder getOrderNonCopy() {
        return order;
    }

    /**
     * Sets the suit order to a copy of the given suit order
     *
     * @param so provided new suit order
     */
    @Contract(mutates = "this")
    public void setOrder(@NotNull SuitOrder so) {
        order = so.copy();
    }

    /**
     * Determines whether the deck is sorted
     *
     * @return Whether the deck of cards is sorted given current suit order
     */
    @Contract(pure = true)
    public boolean isSorted() {
        for (int i = cards.size() - 1; i > 0; i--) {
            if (order.compare(cards.get(i), cards.get(i - 1)) < 0) return false;
        }
        return true;
    }

    /**
     * Gets card at index i
     *
     * @param i Index to get card at
     * @return Card at index i
     * @throws IndexOutOfBoundsException if i < 0 or i >= size()
     */
    @Contract(pure = true)
    @NotNull
    public Card get(int i) {
        return cards.get(i);
    }

    /**
     * Clears the deck
     */
    @Contract(mutates = "this")
    public void clear() {
        cards.clear();
    }

    /**
     * Sorts the deck of cards with the internal suit order
     */
    @Contract(mutates = "this")
    public void sort() {
        cards.sort(order);
    }

    /**
     * Returns the number of cards in the deck
     *
     * @return number of cards in the deck
     */
    @Contract(pure = true)
    public int size() {
        return cards.size();
    }

    /**
     * Returns whether the deck is empty or not
     *
     * @return true if the deck is empty
     */
    @Contract(pure = true)
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Adds a card to this deck after copying it
     *
     * @param card card added
     */
    @Contract(mutates = "this")
    public void add(@NotNull Card card) {
        cards.add(card.copy());
    }

    /**
     * Adds all cards from another deck to this deck after making copies of them
     *
     * @param deck other deck
     */
    @Contract(mutates = "this")
    public void add(@NotNull Deck deck) {
        for (Card c : deck.cards) {
            cards.add(c.copy());
        }
    }

    /**
     * Removes a card from the deck
     *
     * @param card card to be removed
     * @return true if removal was successful
     */
    @Contract(mutates = "this")
    public boolean remove(Card card) {
        return cards.remove(card);
    }

    /**
     * Removes all cards that does not match the given suit
     *
     * @param suit Suit by which all cards are removed
     */
    @Contract(mutates = "this")
    public void removeNonMatchingSuit(Suit suit) {
        cards.removeIf(t -> !t.getSuit().equals(suit));
    }

    /**
     * Removes all non-penalty cards
     */
    @Contract(mutates = "this")
    public void removeNonPenaltyCards() {
        cards.removeIf(t -> !t.isPenaltyCard());
    }

    /**
     * Checks whether  the deck contains the three of clubs
     *
     * @return true if deck contains the 3 of clubs
     */
    @Contract(pure = true)
    public boolean containsThreeOfClubs() {
        return cards.contains(new Card(Suit.Club, 3));
    }

    /**
     * Checks whether the deck contains the 10 of clubs
     *
     * @return true if deck contains the 10 of clubs
     */
    @Contract(pure = true)
    public boolean containsTenOfClubs() {
        return cards.contains(new Card(Suit.Club, 10));
    }

    /**
     * Checks whether the deck contains a card of the given suit
     *
     * @return true if deck contains card of the given suit
     */
    @Contract(pure = true)
    public boolean containsSuit(Suit a) {
        for (Card c : cards) {
            if (c.getSuit().equals(a)) return true;
        }
        return false;
    }

    /**
     * Checks whether the deck contains a given card
     *
     * @param card Card in question
     * @return Whether this deck contains the card
     */
    @Contract(pure = true)
    public boolean contains(Card card) {
        return cards.contains(card);
    }

    /**
     * Distributes all 52 cards in this given deck into the provided decks
     *
     * @throws IllegalArgumentException if deck size != 52 or given decks are not empty
     */
    @Contract(mutates = "this, param1, param2, param3, param4")
    public void randomlyDistribute(@NotNull Deck d1, @NotNull Deck d2, @NotNull Deck d3, @NotNull Deck d4) {
        if (cards.size() != 52) throw new IllegalArgumentException();
        if (!d1.isEmpty() || !d2.isEmpty() || !d3.isEmpty() || !d4.isEmpty()) throw new IllegalArgumentException();
        Random random = new Random();
        for (int i = 13; i > 0; i--) {
            int index = random.nextInt(i * 4);
            Card card = cards.get(index);
            cards.remove(index);
            d1.add(card);
            index = random.nextInt(i * 4 - 1);
            card = cards.get(index);
            cards.remove(index);
            d2.add(card);
            index = random.nextInt(i * 4 - 2);
            card = cards.get(index);
            cards.remove(index);
            d3.add(card);
            index = random.nextInt(i * 4 - 3);
            card = cards.get(index);
            cards.remove(index);
            d4.add(card);
        }
        assert (d1.size() == 13);
        assert (d2.size() == 13);
        assert (d3.size() == 13);
        assert (d4.size() == 13);
        assert (cards.isEmpty());
    }

    /**
     * Returns a copy of all playable cards in the deck (all cards of the suit, or all of them if suit not found)
     *
     * @param suit Active suit
     * @return Copy of all cards that are playable
     */
    @Contract(pure = true)
    @NotNull
    public Deck getPlayableCards(@NotNull Suit suit) {
        if (!containsSuit(suit)) return copy();
        Deck play = new Deck();
        for (Card c : cards) {
            if (c.getSuit().equals(suit)) play.add(c);
        }
        return play;
    }

    /**
     * @return number of penalty cards in the deck
     */
    @Contract(pure = true)
    public int numPenaltyCards() {
        int value = 0;
        for (Card c : cards) {
            if (c.isPenaltyCard()) value++;
        }
        return value;
    }

    /**
     * @return number of penalty points in the deck
     */
    @Contract(pure = true)
    public int penaltyPoints() {
        if (containsTenOfClubs() && numPenaltyCards() == 1) return -50;
        int value = 0;
        if (isShootPossible()) {
            if (containsTenOfClubs()) {
                for (Card c : cards) {
                    if (c.isPenaltyCard() && !c.is10C()) {
                        value += -Math.abs(c.getPenaltyPoints()) * 2;
                    }
                }
            } else {
                for (Card c : cards) {
                    if (c.isPenaltyCard()) {
                        value -= Math.abs(c.getPenaltyPoints());
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
                for (Card c : cards) {
                    if (c.isPenaltyCard()) value += c.getPenaltyPoints();
                }
            }
        }
        return value;
    }

    /**
     * @return true if a shoot is possible (i.e. deck contains all hearts)
     */
    @Contract(pure = true)
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

    /**
     * Fills this deck with 52 cards
     */
    @Contract(mutates = "this")
    public void generate52() {
        clear();
        for (Suit s : Suit.values()) {
            for (int i = 2; i < 11; i++) {
                cards.add(new Card("" + i + s.toString()));
            }
            for (Face f : Face.values()) {
                cards.add(new Card(f.toString() + s));
            }
        }
        assert isValid();
    }

    @Override
    @Contract(pure = true)
    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            sb.append(cards.get(i).toString());
            if (i != cards.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * @return Checks whether every card is valid, but does not check for duplicates
     */
    @Contract(pure = true)
    public boolean isValid() {
        for (Card c : cards) {
            if (!c.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the index of the highest value card of a given suit
     *
     * @param suit Suit in question
     * @return Index of highest valued card (i.e. including face cards)
     */
    @Contract(pure = true)
    public int highestIndexOfSuit(@NotNull Suit suit) {
        int index = -1;
        int value = -1;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getSuit().equals(suit)) {
                if (cards.get(i).getValue() > value) {
                    value = cards.get(i).getValue();
                    index = i;
                }
            }
        }
        return index;
    }

    // EFFECTS: returns a copy of the deck (i.e. all internal components are new copies)

    /**
     * Creates a deep copy of this deck
     *
     * @return deep copy of this deck
     */
    @Contract(pure = true)
    @NotNull
    public Deck copy() {
        Deck deck = new Deck();
        deck.add(this);
        deck.setOrder(this.order);
        return deck;
    }

    @Override
    @Contract(pure = true)
    public boolean equals(Object o) {
        if (!(o instanceof Deck)) return false;
        Deck deck = (Deck) o;
        return deck.cards.equals(this.cards) && this.order.equals(deck.order);
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
        return Objects.hash(order, cards);
    }

    @Override
    public @NotNull Iterator<Card> iterator() {
        return cards.iterator();
    }
}
