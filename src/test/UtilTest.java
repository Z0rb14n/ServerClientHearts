import org.junit.jupiter.api.Test;
import util.Card;
import util.Face;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.Face.*;
import static util.Util.faceGreaterThan;
import static util.Util.faceLessThan;

public class UtilTest {
    private static final Face[] faces = {Jack, Queen, King, Ace};
    private static final Card[] cards = {new Card("3C"), new Card("JC"), new Card("QC"), new Card("KC"), new Card("AC")};

    @Test
    public void testGreaterThan() {
        for (int i = 0; i < faces.length; i++) {
            for (int j = 0; j < faces.length; j++) {
                assertEquals(i > j, faceGreaterThan(faces[i], faces[j]));
            }
        }
    }

    @Test
    public void testLessThan() {
        for (int i = 0; i < faces.length; i++) {
            for (int j = 0; j < faces.length; j++) {
                assertEquals(i < j, faceLessThan(faces[i], faces[j]));
            }
        }
    }

    @Test
    public void testCardGreaterThan() {
        for (int i = 0; i < cards.length; i++) {
            for (int j = 0; j < cards.length; j++) {
                assertEquals(i > j, faceGreaterThan(cards[i], cards[j]));
            }
        }
    }

    @Test
    public void testCardLessThan() {
        for (int i = 0; i < cards.length; i++) {
            for (int j = 0; j < cards.length; j++) {
                assertEquals(i < j, faceLessThan(cards[i], cards[j]));
            }
        }
    }
}
