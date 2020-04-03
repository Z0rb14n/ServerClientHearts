package main.util;

import java.util.ArrayList;
import java.util.Random;

public class Deck {
    private SuitOrder order;
    private final ArrayList<Card> cards;
    
    public Deck() {
        order = new SuitOrder();
        cards = new ArrayList<>(13);
    }
    
    public SuitOrder getOrder() {
        return order;
    }
    
    public void setOrder(SuitOrder so) {
        order = so;
    }
    
    public void sort() {
        cards.sort(order);
    }
    
    public int deckSize() {
        return cards.size();
    }
    public boolean isEmpty() {
        return cards.isEmpty();
    }
    
    public void addCard(Card a) {
        cards.add(a);
    }
    
    public void removeCard(Card a) {
        cards.remove(a);
    }
    public boolean containsThreeOfClubs() {
        return cards.contains(new Card("3C"));
    }
    public boolean containsTenOfClubs() {
        return cards.contains(new Card("10C"));
    }
    public boolean containsSuit(Suit a) {
        for(Card c : cards) {
            if (c.getSuit().equals(a)) return true;
        }
        return false;
    }
    /**
     * Distributes the 52 cards in the deck randomly into d1,d2,d3 and d4
     * @param d1
     * @param d2
     * @param d3
     * @param d4 
     */
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
    /**
     * Returns all playable cards (NOTE: THE CARDS ARE SHALLOW COPIES OF THE PARENT DECK)
     * @param a
     * @return 
     */
    public Deck getPlayableCards(Suit a) {
        if (!containsSuit(a)) return copy();
        Deck play = new Deck();
        for(Card c : cards) {
            if (c.getSuit().equals(a)) play.addCard(c);
        }
        return play;
    }
    public int numPenaltyCards() {
        int value = 0;
        for(Card c : cards) {
            if (c.isPenaltyCard()) value++;
        }
        return value;
    }
    public int penaltyPoints() {
        if (containsTenOfClubs() && numPenaltyCards() == 1) return -50;
        int value = 0;
        if (isShootPossible()) {
            if (containsTenOfClubs()) {
                for(Card c : cards) {
                    if (c.isPenaltyCard() && !c.is10C()) {
                        int pen = c.getPenaltyPoints();
                        if (pen < 0) value += pen * 2;
                        else value -= pen * 2;
                    }
                }
            } else {
                for(Card c : cards) {
                    if (c.isPenaltyCard() && !c.is10C()) {
                        int pen = c.getPenaltyPoints();
                        if (pen < 0) value += pen;
                        else value -= pen;
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
    public boolean isShootPossible() {
        boolean[] contains = new boolean[13];
        for (Card c : cards) {
            if (c.isHeart()) {
                contains[Util.getValue(c)-2] = true;
            }
        }
        for (boolean b : contains) {
            if (!b) return false;
        }
        return true;
    }
    public void generate52() {
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            sb.append(cards.get(i).toString());
            if (i != cards.size() -1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
    public boolean isValid() {
        for (Card c: cards) {
            if (!c.isValid()) {
                return false;
            }
        }
        return true; // DOES NOT CHECK DUPLICATES
    }
    public Deck copy() {
        Deck deck = new Deck();
        for (Card a: cards) {
            deck.addCard(a);
        }
        return deck;
    }
}
