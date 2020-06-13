import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static util.PlayOrder.ASCENDING_NUM;
import static util.PlayOrder.DESCENDING_NUM;

class PlayOrderTest {
    @Test
    void testNextPlayer() {
        assertEquals(4, ASCENDING_NUM.nextPlayer(3));
        assertEquals(3, ASCENDING_NUM.nextPlayer(2));
        assertEquals(2, ASCENDING_NUM.nextPlayer(1));
        assertEquals(1, ASCENDING_NUM.nextPlayer(4));

        assertEquals(2, DESCENDING_NUM.nextPlayer(3));
        assertEquals(1, DESCENDING_NUM.nextPlayer(2));
        assertEquals(4, DESCENDING_NUM.nextPlayer(1));
        assertEquals(3, DESCENDING_NUM.nextPlayer(4));

        try {
            assertEquals(2, ASCENDING_NUM.nextPlayer(-1));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            assertEquals(2, ASCENDING_NUM.nextPlayer(5));
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void testNextMultiplePlayer() {
        assertEquals(4, ASCENDING_NUM.nextPlayer(2, 2));
        assertEquals(4, ASCENDING_NUM.nextPlayer(1, 3));
        assertEquals(4, ASCENDING_NUM.nextPlayer(3, 1));
        assertEquals(4, ASCENDING_NUM.nextPlayer(4, 0));

        assertEquals(3, ASCENDING_NUM.nextPlayer(3, 0));
        assertEquals(3, ASCENDING_NUM.nextPlayer(2, 1));
        assertEquals(3, ASCENDING_NUM.nextPlayer(1, 2));
        assertEquals(3, ASCENDING_NUM.nextPlayer(4, 3));

        assertEquals(2, ASCENDING_NUM.nextPlayer(3, 3));
        assertEquals(2, ASCENDING_NUM.nextPlayer(2, 0));
        assertEquals(2, ASCENDING_NUM.nextPlayer(1, 1));
        assertEquals(2, ASCENDING_NUM.nextPlayer(4, 2));

        assertEquals(1, ASCENDING_NUM.nextPlayer(3, 2));
        assertEquals(1, ASCENDING_NUM.nextPlayer(2, 3));
        assertEquals(1, ASCENDING_NUM.nextPlayer(1, 0));
        assertEquals(1, ASCENDING_NUM.nextPlayer(4, 1));

        assertEquals(4, DESCENDING_NUM.nextPlayer(1, 1));
        assertEquals(4, DESCENDING_NUM.nextPlayer(2, 2));
        assertEquals(4, DESCENDING_NUM.nextPlayer(3, 3));
        assertEquals(4, DESCENDING_NUM.nextPlayer(4, 0));

        assertEquals(3, DESCENDING_NUM.nextPlayer(3, 0));
        assertEquals(3, DESCENDING_NUM.nextPlayer(2, 3));
        assertEquals(3, DESCENDING_NUM.nextPlayer(1, 2));
        assertEquals(3, DESCENDING_NUM.nextPlayer(4, 1));

        assertEquals(2, DESCENDING_NUM.nextPlayer(3, 1));
        assertEquals(2, DESCENDING_NUM.nextPlayer(2, 0));
        assertEquals(2, DESCENDING_NUM.nextPlayer(1, 3));
        assertEquals(2, DESCENDING_NUM.nextPlayer(4, 2));

        assertEquals(1, DESCENDING_NUM.nextPlayer(3, 2));
        assertEquals(1, DESCENDING_NUM.nextPlayer(2, 1));
        assertEquals(1, DESCENDING_NUM.nextPlayer(1, 0));
        assertEquals(1, DESCENDING_NUM.nextPlayer(4, 3));

        try {
            assertEquals(-1, DESCENDING_NUM.nextPlayer(5, 0));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            assertEquals(-1, DESCENDING_NUM.nextPlayer(0, 0));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            assertEquals(-1, DESCENDING_NUM.nextPlayer(4, -1));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            assertEquals(-1, DESCENDING_NUM.nextPlayer(4, 4));
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void testBadPlayerHasPlayed() {
        try {
            assertFalse(ASCENDING_NUM.hasPlayerPlayed(0, 0, 1));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            assertFalse(ASCENDING_NUM.hasPlayerPlayed(5, 0, 1));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            assertFalse(ASCENDING_NUM.hasPlayerPlayed(1, -1, 1));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            assertFalse(ASCENDING_NUM.hasPlayerPlayed(1, 4, 1));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            assertFalse(ASCENDING_NUM.hasPlayerPlayed(1, 0, 0));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            assertFalse(ASCENDING_NUM.hasPlayerPlayed(1, 0, 5));
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void testPlayerHasPlayed() {
        for (int i = 1; i < 5; i++) {
            for (int k = 1; k < 5; k++) {
                assertFalse(ASCENDING_NUM.hasPlayerPlayed(i, 0, k));
                assertFalse(DESCENDING_NUM.hasPlayerPlayed(i, 0, k));
            }
        }

        for (int i = 1; i < 5; i++) {
            for (int k = 1; k < 5; k++) {
                assertEquals(i == k, ASCENDING_NUM.hasPlayerPlayed(i, 1, k));
                assertEquals(i == k, DESCENDING_NUM.hasPlayerPlayed(i, 1, k));
            }
        }

        assertTrue(ASCENDING_NUM.hasPlayerPlayed(3, 2, 4));
        assertFalse(ASCENDING_NUM.hasPlayerPlayed(3, 2, 1));

        assertFalse(DESCENDING_NUM.hasPlayerPlayed(3, 2, 4));
        assertFalse(DESCENDING_NUM.hasPlayerPlayed(3, 2, 1));
        assertTrue(DESCENDING_NUM.hasPlayerPlayed(3, 3, 1));
    }
}
