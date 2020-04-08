import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.Suit.*;

public class SuitTest {
    @Test
    public void testToString() {
        assertEquals("C", Club.toString());
        assertEquals("S", Spade.toString());
        assertEquals("D", Diamond.toString());
        assertEquals("H", Heart.toString());
    }
}
