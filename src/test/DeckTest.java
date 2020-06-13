import org.junit.jupiter.api.Test;
import util.Card;
import util.Deck;
import util.SuitOrder;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static util.Suit.*;

// TODO FINISH TESTS

class DeckTest {
    @Test
    void testGenerate() {
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
    void testSerializable() {
        Deck deck = new Deck();
        deck.generate52();
        try {
            File file = new File("./data/testSerializable.txt");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(deck);
            oos.flush();
            oos.close();

            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Deck deck2 = (Deck) ois.readObject();
            assertEquals(deck2, deck);
            ois.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testSuitOrder() {
        SuitOrder so = new SuitOrder();
        Deck deck = new Deck();
        assertEquals(so, deck.getOrder());
        try {
            deck.setOrder(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        so.moveSuitToLocation(Diamond, 1);
        assertNotEquals(so, deck.getOrder());
        deck.setOrder(so);
        assertEquals(so, deck.getOrder());
    }

    @Test
    void testClear() {
        Deck deck = new Deck();
        deck.generate52();
        assertEquals(52, deck.deckSize());
        deck.clear();
        assertEquals(0, deck.deckSize());
    }

    @Test
    void testAdd() {
        Deck deck = new Deck();
        deck.addCard(new Card("3C"));
        assertEquals(new Card("3C"), deck.get(0));
        assertTrue(deck.contains(new Card("3C")));
        assertTrue(deck.containsSuit(Club));
        assertTrue(deck.containsThreeOfClubs());
        deck.clear();
        Deck deck2 = new Deck();
        deck2.generate52();
        deck.addAll(deck2);
        assertEquals(deck, deck2);
        assertEquals(52, deck.deckSize());
    }

    @Test
    void testRemove() {
        Deck deck = new Deck();
    }

    @Test
    void testIterator() {
        Deck deck = new Deck();
        deck.generate52();
        int i = 0;
        for (Card c : deck) {
            assertEquals(c, deck.get(i));
            i++;
        }
    }
}
