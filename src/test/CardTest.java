import org.junit.jupiter.api.Test;
import util.Card;
import util.Suit;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {
    private Card card;

    @Test
    public void testInit() {
        try {
            card = new Card("3C");
            Card card2 = new Card("3C");
            assertEquals(card, card2);
            assertEquals(Suit.Club, card.getSuit());
            assertEquals(3, card.getNumber());
            assertTrue(card.isValid());
            assertFalse(card.isPenaltyCard());
            assertFalse(card.isHeart());
            assertNull(card.getFace());
            assertEquals(0, card.getPenaltyPoints());
            assertFalse(card.isFaceCard());
            assertFalse(card.is10C());
            assertEquals("3C", card.toString());
            assertTrue(card.equalFace(card2));
            assertTrue(card.equalNumber(card2));
            assertTrue(card.equalSuit(card2));
        } catch (IllegalArgumentException e) {
            fail("Exception should not have been thrown");
        }
    }

    @Test
    public void testGetValue() {
        for (int i = 2; i < 11; i++) {
            String name = i + "C";
            assertEquals(i, new Card(name).getValue());
        }
        assertEquals(11, new Card("JC").getValue());
        assertEquals(12, new Card("QC").getValue());
        assertEquals(13, new Card("KC").getValue());
        assertEquals(14, new Card("AC").getValue());
    }

    @Test
    public void testIsFaceCard() {
        card = new Card("JH");
        assertTrue(card.isFaceCard());
        card = new Card("3C");
        assertFalse(card.isFaceCard());
    }

    @Test
    public void testIsPenaltyCard() {
        card = new Card("2H");
        assertFalse(card.isPenaltyCard());
        card = new Card("5H");
        assertTrue(card.isPenaltyCard());
        card = new Card("10H");
        assertTrue(card.isPenaltyCard());
        card = new Card("JH");
        assertTrue(card.isPenaltyCard());
        card = new Card("AH");
        assertTrue(card.isPenaltyCard());
        card = new Card("QS");
        assertTrue(card.isPenaltyCard());
        card = new Card("JD");
        assertTrue(card.isPenaltyCard());
        card = new Card("10C");
        assertTrue(card.isPenaltyCard());
        card = new Card("4C");
        assertFalse(card.isPenaltyCard());
    }

    @Test
    public void testPenaltyPoints() {
        card = new Card("2H");
        assertEquals(0, card.getPenaltyPoints());
        card = new Card("5H");
        assertEquals(10, card.getPenaltyPoints());
        card = new Card("10H");
        assertEquals(10, card.getPenaltyPoints());
        card = new Card("JH");
        assertEquals(20, card.getPenaltyPoints());
        card = new Card("AH");
        assertEquals(50, card.getPenaltyPoints());
        card = new Card("QS");
        assertEquals(100, card.getPenaltyPoints());
        card = new Card("JD");
        assertEquals(-100, card.getPenaltyPoints());
        card = new Card("10C");
        assertEquals(-50, card.getPenaltyPoints());
        card = new Card("4C");
        assertEquals(0, card.getPenaltyPoints());
    }

    @Test
    public void testIsHeart() {
        card = new Card("10H");
        Card card2 = new Card("3H");
        Card card3 = new Card("AH");
        Card card4 = new Card("3C");
        assertTrue(card.isHeart());
        assertTrue(card2.isHeart());
        assertTrue(card3.isHeart());
        assertFalse(card4.isHeart());
    }

    @Test
    public void testIs10C() {
        card = new Card("10C");
        Card card2 = new Card("3C");
        assertTrue(card.is10C());
        assertFalse(card2.is10C());
    }

    @Test
    public void testEqualSuit() {
        card = new Card("3C");
        Card card2 = new Card("JC");
        Card card3 = new Card("3H");
        Card card4 = new Card("JH");
        Card card5 = new Card("5C");
        assertTrue(card.equalSuit(card2));
        assertFalse(card.equalSuit(card3));
        assertFalse(card.equalSuit(card4));
        assertTrue(card.equalSuit(card5));
    }

    @Test
    public void testEqualFace() {
        card = new Card("JC");
        Card card2 = new Card("KC");
        Card card3 = new Card("3H");
        Card card4 = new Card("JH");
        Card card5 = new Card("3C");
        assertTrue(card.equalFace(card4));
        assertFalse(card.equalFace(card5));
        assertTrue(card3.equalFace(card5));
        assertFalse(card.equalFace(card2));
    }

    @Test
    public void testEqualNumber() {
        card = new Card("3C");
        Card card2 = new Card("3H");
        Card card3 = new Card("JC");
        Card card4 = new Card("JH");
        assertTrue(card.equalNumber(card2));
        assertFalse(card.equalNumber(card3));
        assertTrue(card4.equalNumber(card3));
    }

    @Test
    public void lol() {
        String input = "CARDS:3C,4C,5C".substring(6);
        Scanner scanner = new Scanner(input).useDelimiter(",");
        while (scanner.hasNext()) {
            System.out.println(scanner.next());
        }
    }
}
