import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Card;
import util.Suit;
import util.SuitOrder;

import static org.junit.jupiter.api.Assertions.*;
import static util.Suit.*;

// TODO FINISH TESTS

class SuitOrderTest {
    private static final Card[] cards = new Card[]{
            new Card("3C"), new Card("4C"),
            new Card("3H"), new Card("4H"),
            new Card("3S"), new Card("4S"),
            new Card("3D"), new Card("4D")
    };
    private SuitOrder so;

    @BeforeEach
    void runBefore() {
        so = new SuitOrder();
        assertFalse(so.isSortingByValue());
        assertArrayEquals(so.getSuitOrder(), SuitOrder.DEFAULT); // H > D  > S > C
    }

    @Test
    void testReset() {
        so.setSortByValue(true);
        so.moveSuitToBottom(Heart);
        assertTrue(so.isSortingByValue());
        assertArrayEquals(so.getSuitOrder(), new Suit[]{Diamond, Spade, Club, Heart});
        so.reset();
        assertFalse(so.isSortingByValue());
        assertArrayEquals(so.getSuitOrder(), SuitOrder.DEFAULT);
    }

    @Test
    void testLocateSuit() {
        assertEquals(1, so.locateSuit(Heart));
        assertEquals(2, so.locateSuit(Diamond));
        assertEquals(3, so.locateSuit(Spade));
        assertEquals(4, so.locateSuit(Club));
    }

    @Test
    void testValueCompare() {
        assertEquals(0, so.valueCompare(cards[0], cards[2]));
        assertEquals(1, so.valueCompare(cards[1], cards[0]));
        assertEquals(-1, so.valueCompare(cards[4], cards[5]));
    }

    @Test
    void testDefaultSuitCompare() {
        assertEquals(0, so.suitCompare(Heart, Heart));
        assertEquals(1, so.suitCompare(Heart, Diamond));
        assertEquals(1, so.suitCompare(Heart, Spade));
        assertEquals(1, so.suitCompare(Heart, Club));
        assertEquals(-1, so.suitCompare(Diamond, Heart));
        assertEquals(0, so.suitCompare(Diamond, Diamond));
        assertEquals(1, so.suitCompare(Diamond, Spade));
        assertEquals(1, so.suitCompare(Diamond, Club));
        assertEquals(-1, so.suitCompare(Spade, Heart));
        assertEquals(-1, so.suitCompare(Spade, Diamond));
        assertEquals(0, so.suitCompare(Spade, Spade));
        assertEquals(1, so.suitCompare(Spade, Club));
        assertEquals(-1, so.suitCompare(Club, Heart));
        assertEquals(-1, so.suitCompare(Club, Diamond));
        assertEquals(-1, so.suitCompare(Club, Spade));
        assertEquals(0, so.suitCompare(Club, Club));
    }

    @Test
    void testDefaultSuitCompareCards() {
        assertEquals(0, so.suitCompare(cards[3], cards[3]));
        assertEquals(1, so.suitCompare(cards[3], cards[7]));
        assertEquals(1, so.suitCompare(cards[3], cards[5]));
        assertEquals(1, so.suitCompare(cards[3], cards[0]));
        assertEquals(-1, so.suitCompare(cards[7], cards[3]));
        assertEquals(0, so.suitCompare(cards[7], cards[7]));
        assertEquals(1, so.suitCompare(cards[7], cards[5]));
        assertEquals(1, so.suitCompare(cards[7], cards[0]));
        assertEquals(-1, so.suitCompare(cards[5], cards[3]));
        assertEquals(-1, so.suitCompare(cards[5], cards[7]));
        assertEquals(0, so.suitCompare(cards[5], cards[5]));
        assertEquals(1, so.suitCompare(cards[5], cards[0]));
        assertEquals(-1, so.suitCompare(cards[0], cards[3]));
        assertEquals(-1, so.suitCompare(cards[0], cards[7]));
        assertEquals(-1, so.suitCompare(cards[0], cards[5]));
        assertEquals(0, so.suitCompare(cards[0], cards[0]));
    }

    @Test
    void testCompareDefault() {
        for (int i = 0; i < cards.length; i++) {
            for (int j = 0; j < cards.length; j++) {
                if (i == j) {
                    assertEquals(0, so.compare(cards[i], cards[j]));
                } else {
                    if (so.suitCompare(cards[i], cards[j]) == 0) {
                        assertEquals(so.valueCompare(cards[i], cards[j]), so.compare(cards[i], cards[j]));
                    } else {
                        assertEquals(so.suitCompare(cards[i], cards[j]), so.compare(cards[i], cards[j]));
                    }
                }
            }
        }
    }

    @Test
    void testMoveSuitToPosition() {
        so.moveSuitToLocation(Heart, 1);
        assertArrayEquals(so.getSuitOrder(), SuitOrder.DEFAULT); // H > D > S > C
        so.moveSuitToLocation(Spade, 1);                 // S > H > D > C
        assertArrayEquals(so.getSuitOrder(), new Suit[]{Spade, Heart, Diamond, Club});
        so.moveSuitToLocation(Heart, 4);                 // S > D > C > H
        assertArrayEquals(so.getSuitOrder(), new Suit[]{Spade, Diamond, Club, Heart});
        testCompareDefault();
        so.moveSuitToTop(Club);
        assertArrayEquals(so.getSuitOrder(), new Suit[]{Club, Spade, Diamond, Heart});
        testCompareDefault();
    }

    @Test
    void testCompareSortValue() {
        so.setSortByValue(true);
        for (Card card : cards) {
            for (Card card1 : cards) {
                assertEquals(so.valueCompare(card, card1), so.compare(card, card1));
            }
        }
    }
}
