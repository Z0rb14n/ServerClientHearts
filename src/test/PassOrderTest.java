import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.PassOrder.*;

class PassOrderTest {
    @Test
    void testNextPlayer() {
        assertEquals(1, ASCENDING_NUM.toPass(4));
        assertEquals(2, ASCENDING_NUM.toPass(1));
        assertEquals(3, ASCENDING_NUM.toPass(2));
        assertEquals(4, ASCENDING_NUM.toPass(3));

        assertEquals(1, DESCENDING_NUM.toPass(2));
        assertEquals(2, DESCENDING_NUM.toPass(3));
        assertEquals(3, DESCENDING_NUM.toPass(4));
        assertEquals(4, DESCENDING_NUM.toPass(1));

        assertEquals(1, ODD_EVEN.toPass(3));
        assertEquals(2, ODD_EVEN.toPass(4));
        assertEquals(3, ODD_EVEN.toPass(1));
        assertEquals(4, ODD_EVEN.toPass(2));

        assertEquals(1, LOW_HIGH.toPass(2));
        assertEquals(2, LOW_HIGH.toPass(1));
        assertEquals(3, LOW_HIGH.toPass(4));
        assertEquals(4, LOW_HIGH.toPass(3));
    }
}
