import org.junit.jupiter.api.Test;
import util.Deck;

import java.io.*;

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
    public void testSerializable() {
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
}
