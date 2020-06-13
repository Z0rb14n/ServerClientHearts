import org.junit.jupiter.api.Test;
import util.ChatMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ChatMessageTest {
    @Test
    void testInit() {
        assertEquals("Player 4: HI", new ChatMessage(4, "HI").toString());
        assertEquals("Player 2: HELLO", new ChatMessage(2, "HELLO").toString());
        try {
            assertEquals("Player 69: HI", new ChatMessage(69, "HI").toString());
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            assertEquals("Player 0: HI", new ChatMessage(0, "HI").toString());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }
}
