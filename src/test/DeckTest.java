import org.junit.jupiter.api.Test;
import util.Card;
import util.Deck;
import util.SuitOrder;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static util.Suit.*;

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
    void testDistribute() {
        Deck deck = new Deck();
        deck.generate52();
        Deck deck2 = new Deck();
        deck2.addAll(deck);
        Deck deck3 = new Deck();
        Deck deck4 = new Deck();
        Deck deck5 = new Deck();
        Deck deck6 = new Deck();
        deck.randomlyDistribute(deck3, deck4, deck5, deck6);
        for (Card c : deck2) {
            assertTrue(deck3.contains(c) || deck4.contains(c) || deck5.contains(c) || deck6.contains(c));
        }

        Deck empty = new Deck();

        try {
            deck.randomlyDistribute(deck3, deck4, deck5, deck6);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            deck2.randomlyDistribute(deck3, empty, empty, empty);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            deck2.randomlyDistribute(empty, deck3, empty, empty);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            deck2.randomlyDistribute(empty, empty, deck3, empty);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            deck2.randomlyDistribute(empty, empty, empty, deck3);
            fail();
        } catch (IllegalArgumentException ignored) {
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
    void testHighestOfGivenSuit() {
        Deck deck = new Deck();
        deck.addCard(new Card("2C"));
        assertEquals(0, deck.highestIndexOfSuit(Club));
        assertEquals(-1, deck.highestIndexOfSuit(Heart));
        deck.addCard(new Card("JS"));
        assertEquals(0, deck.highestIndexOfSuit(Club));
        assertEquals(-1, deck.highestIndexOfSuit(Heart));
        assertEquals(1, deck.highestIndexOfSuit(Spade));
        deck.addCard(new Card("4C"));
        deck.addCard(new Card("3C"));
        assertEquals(2, deck.highestIndexOfSuit(Club));
    }

    @Test
    void testEquals() {
        Deck deck = new Deck();
        Deck deck2 = new Deck();
        assertEquals(deck, deck2);
        deck2.generate52();
        assertNotEquals(deck, deck2);
        deck.addAll(deck2);
        assertEquals(deck, deck2);
        assertEquals(deck.hashCode(), deck2.hashCode());
        assertEquals(deck.copy(), deck2);
        assertNotEquals(new Card("3C"), deck2);
        assertNotEquals(deck2, new Card("3C"));
    }

    @Test
    void testRemoveNonMatchingSuit() {
        Deck deck = new Deck();
        deck.addCard(new Card("3C"));
        deck.addCard(new Card("4C"));
        deck.addCard(new Card("5C"));
        deck.addCard(new Card("6H"));
        Deck deck2 = new Deck();
        deck2.addAll(deck);
        deck2.removeNonMatchingSuit(Heart);
        assertEquals(1, deck2.deckSize());
        assertTrue(deck2.contains(new Card("6H")));
        deck2 = new Deck();
        deck2.addAll(deck);
        deck2.removeNonMatchingSuit(Diamond);
        assertEquals(0, deck2.deckSize());
    }

    @Test
    void testRemoveNonPenalties() {
        Deck deck = new Deck();
        deck.addCard(new Card("3C"));
        deck.addCard(new Card("4C"));
        deck.addCard(new Card("5C"));
        deck.addCard(new Card("6H"));
        deck.removeNonPenaltyCards();
        assertEquals(1, deck.deckSize());
        assertTrue(deck.contains(new Card("6H")));
    }

    @Test
    void testSort() {
        Deck deck = new Deck();
        deck.generate52();
        assertFalse(deck.isSorted());
        deck.sort();
        assertTrue(deck.isSorted());
    }

    @Test
    void testString() {
        Deck deck = new Deck();
        assertEquals("", deck.toString());
        deck.addCard(new Card("3C"));
        assertEquals("3C", deck.toString());
        deck.addCard(new Card("4C"));
        deck.addCard(new Card("5C"));
        assertEquals("3C,4C,5C", deck.toString());
    }

    @Test
    void testPenaltyPoints() {
        final Deck allButOne = new Deck();
        allButOne.addCard(new Card("3H"));
        allButOne.addCard(new Card("4H"));
        allButOne.addCard(new Card("5H"));
        allButOne.addCard(new Card("6H"));
        allButOne.addCard(new Card("7H"));
        allButOne.addCard(new Card("8H"));
        allButOne.addCard(new Card("9H"));
        allButOne.addCard(new Card("10H"));
        allButOne.addCard(new Card("JH"));
        allButOne.addCard(new Card("QH"));
        allButOne.addCard(new Card("KH"));
        allButOne.addCard(new Card("AH"));
        Deck deck = new Deck();
        deck.addCard(new Card("2H"));
        deck.addAll(allButOne);
        assertEquals(-200, deck.penaltyPoints());
        deck = new Deck();
        deck.addAll(allButOne);
        assertEquals(200, deck.penaltyPoints());
        deck = new Deck();
        deck.addCard(new Card("10C"));
        deck.addAll(allButOne);
        assertEquals(400, deck.penaltyPoints());
    }

    @Test
    void testPlayableCards() {
        Deck deck = new Deck();
        deck.addCard(new Card("3C"));
        deck.addCard(new Card("4C"));
        deck.addCard(new Card("5C"));
        deck.addCard(new Card("6H"));
        assertEquals(3, deck.getPlayableCards(Club).deckSize());
        assertTrue(deck.getPlayableCards(Club).containsThreeOfClubs());
        assertTrue(deck.getPlayableCards(Club).contains(new Card("4C")));
        assertTrue(deck.getPlayableCards(Club).contains(new Card("5C")));
        assertFalse(deck.getPlayableCards(Club).contains(new Card("6H")));
        assertFalse(deck.containsSuit(Diamond));
        assertEquals(4, deck.getPlayableCards(Diamond).deckSize());
    }

    @Test
    void testRemove() {
        Deck deck = new Deck();
        deck.generate52();
        deck.removeCard(new Card("3C"));
        assertFalse(deck.contains(new Card("3C")));
        assertFalse(deck.containsThreeOfClubs());
        assertEquals(51, deck.deckSize());
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
