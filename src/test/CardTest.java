import org.junit.jupiter.api.Test;
import util.Card;
import util.Suit;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {
    private Card card;

    @Test
    void testInit() {
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
    void testBadInit() {
        try {
            new Card("4F");
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new Card("1D");
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new Card("LD");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            new Card("4FD");
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new Card("4");
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new Card("4FDA");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void testGetValue() {
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
    void testIsFaceCard() {
        card = new Card("JH");
        assertTrue(card.isFaceCard());
        card = new Card("3C");
        assertFalse(card.isFaceCard());
    }

    @Test
    void testIsPenaltyCard() {
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
    void testPenaltyPoints() {
        assertEquals(0, new Card("2H").getPenaltyPoints());
        assertEquals(10, new Card("5H").getPenaltyPoints());
        assertEquals(10, new Card("10H").getPenaltyPoints());
        assertEquals(20, new Card("JH").getPenaltyPoints());
        assertEquals(50, new Card("AH").getPenaltyPoints());
        assertEquals(0, new Card("3S").getPenaltyPoints());
        assertEquals(100, new Card("QS").getPenaltyPoints());
        assertEquals(-100, new Card("JD").getPenaltyPoints());
        assertEquals(0, new Card("QD").getPenaltyPoints());
        assertEquals(-50, new Card("10C").getPenaltyPoints());
        assertEquals(0, new Card("4C").getPenaltyPoints());
    }

    @Test
    void testIsHeart() {
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
    void testIs10C() {
        card = new Card("10C");
        Card card2 = new Card("3C");
        assertTrue(card.is10C());
        assertFalse(card2.is10C());
        assertFalse(new Card("10D").is10C());
    }

    @Test
    void testIs3C() {
        assertTrue(new Card("3C").is3C());
        assertFalse(new Card("4C").is3C());
        assertFalse(new Card("3H").is3C());
    }
    @Test
    void testEqualSuit() {
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
    void testEqualFace() {
        card = new Card("JC");
        Card card2 = new Card("KC");
        Card card3 = new Card("3H");
        Card card4 = new Card("JH");
        Card card5 = new Card("3C");
        assertTrue(card.equalFace(card4));
        assertFalse(card.equalFace(card5));
        assertTrue(card3.equalFace(card5));
        assertFalse(card3.equalFace(card4));
        assertFalse(card.equalFace(card2));
    }

    @Test
    void testEqualNumber() {
        card = new Card("3C");
        Card card2 = new Card("3H");
        Card card3 = new Card("JC");
        Card card4 = new Card("JH");
        assertTrue(card.equalNumber(card2));
        assertFalse(card.equalNumber(card3));
        assertTrue(card4.equalNumber(card3));
    }

    @Test
    void testToString() {
        Card card = new Card("3C");
        assertEquals("3C", card.toString());
        Card card2 = new Card("JH");
        assertEquals("JH", card2.toString());
    }

    @Test
    void testEquals() {
        assertNotEquals("3C", new Card("3C"));
        assertNotEquals(new Card("3C"), "3C");
        assertEquals(new Card("3C"), new Card("3C"));
        assertNotEquals(new Card("4C"), new Card("3C"));
        assertNotEquals(new Card("3C"), new Card("JC"));
        assertNotEquals(new Card("JC"), new Card("3C"));
        assertNotEquals(new Card("3C"), new Card("3H"));
        assertNotEquals(new Card("QC"), new Card("JC"));
        assertEquals(new Card("JC"), new Card("JC"));
    }

    @Test
    void testCopy() {
        assertEquals(new Card("3C"), new Card("3C").copy());
        assertEquals(new Card("3C").hashCode(), new Card("3C").copy().hashCode());
        assertEquals(new Card("JH").hashCode(), new Card("JH").copy().hashCode());
    }
}
