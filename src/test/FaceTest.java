import org.junit.jupiter.api.Test;
import util.Face;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class FaceTest {
    @Test
    void testToString() {
        assertEquals("J", Face.Jack.toString());
        assertEquals("Q", Face.Queen.toString());
        assertEquals("K", Face.King.toString());
        assertEquals("A", Face.Ace.toString());
    }

    @Test
    void testGetFace() {
        assertEquals(Face.Jack, Face.getFace('J'));
        assertEquals(Face.Queen, Face.getFace('Q'));
        assertEquals(Face.King, Face.getFace('K'));
        assertEquals(Face.Ace, Face.getFace('A'));
        try {
            assertEquals(Face.Ace, Face.getFace('L'));
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void testGetValue() {
        assertEquals(11, Face.Jack.getValue());
        assertEquals(12, Face.Queen.getValue());
        assertEquals(13, Face.King.getValue());
        assertEquals(14, Face.Ace.getValue());
    }
}
