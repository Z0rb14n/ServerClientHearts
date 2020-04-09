import org.junit.jupiter.api.Test;
import util.Card;
import util.Deck;
import util.SuitOrder;

import static org.junit.jupiter.api.Assertions.*;
import static util.Suit.*;

public class DeckTest {
    @Test
    public void testGenerate() {
        Deck deck = new Deck();
        deck.generate52();
        assertEquals(52, deck.deckSize());
        assertTrue(deck.isValid());
        assertTrue(deck.isShootPossible());
        assertFalse(deck.isEmpty());
        assertTrue(deck.containsTenOfClubs());
        assertTrue(deck.containsThreeOfClubs());
        assertTrue(deck.containsSuit(Club));
        assertTrue(deck.containsSuit(Spade));
        assertTrue(deck.containsSuit(Heart));
        assertTrue(deck.containsSuit(Diamond));
        assertEquals(13, deck.numPenaltyCards());
        assertEquals(-800, deck.penaltyPoints());
    }

    @Test
    public void lol() {
        Deck deck = new Deck();
        SuitOrder so = new SuitOrder();
        so.setSortByValue(true);
        deck.setOrder(so);
        deck.generate52();
        deck.sort();
        for (Card c : deck) {
            System.out.print(c);
        }
    }
}
