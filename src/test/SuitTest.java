import org.junit.jupiter.api.Test;
import util.Suit;

import static net.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static util.Suit.*;

class SuitTest {
    @Test
    void testToString() {
        assertEquals("C", Club.toString());
        assertEquals("S", Spade.toString());
        assertEquals("D", Diamond.toString());
        assertEquals("H", Heart.toString());
    }

    @Test
    void testCharacter() {
        assertEquals(HEART_UNICODE, Heart.getCharacter());
        assertEquals(DIAMOND_UNICODE, Diamond.getCharacter());
        assertEquals(SPADE_UNICODE, Spade.getCharacter());
        assertEquals(CLUB_UNICODE, Club.getCharacter());
    }

    @Test
    void testGetSuit() {
        assertEquals(Club, Suit.getSuit("C"));
        assertEquals(Spade, Suit.getSuit("S"));
        assertEquals(Diamond, Suit.getSuit("D"));
        assertEquals(Heart, Suit.getSuit("H"));
        try {
            assertEquals(Club, Suit.getSuit("LOL"));
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }
}
