import org.junit.jupiter.api.Test;
import util.Face;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FaceTest {
    @Test
    public void testToString() {
        assertEquals("J", Face.Jack.toString());
        assertEquals("Q", Face.Queen.toString());
        assertEquals("K", Face.King.toString());
        assertEquals("A", Face.Ace.toString());
    }

    @Test
    public void testGetValue() {
        assertEquals(11, Face.Jack.getValue());
        assertEquals(12, Face.Queen.getValue());
        assertEquals(13, Face.King.getValue());
        assertEquals(14, Face.Ace.getValue());
    }
}
