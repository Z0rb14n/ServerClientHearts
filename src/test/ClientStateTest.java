import net.MessageConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import processing.core.PImage;
import util.ClientState;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static ui.ServerClientHeartsClient.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientStateTest {
    ClientState sc;

    @BeforeAll
    public void runBeforeAll() throws IOException {
        CAT_OUTLINE = new PImage(ImageIO.read(new File(CAT_OUTLINE_FILE)));
        CAT_BACK_ONLY = new PImage(ImageIO.read(new File(CAT_BACK_FILE)));
        CAT_FACE_LEFT = new PImage(ImageIO.read(new File(CAT_LEFT_FILE)));
        CAT_FACE_RIGHT = new PImage(ImageIO.read(new File(CAT_RIGHT_FILE)));
        CAT_DEFAULT = new PImage(ImageIO.read(new File(DEFAULT_CAT_FILE)));
    }

    @BeforeEach
    public void runBefore() {
        sc = new ClientState();
    }

    @Test
    public void testConnection() {
        sc.setPlayerNum(1);
        sc.processNewMessage(MessageConstants.CURRENT_PLAYERS_HEADER + "NONE");
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
    }

    @Test
    public void testConnectionNonOneStart() {
        testConnection();
        runBefore();
        sc.setPlayerNum(2);
        sc.processNewMessage(MessageConstants.CURRENT_PLAYERS_HEADER + "NONE");
        assertArrayEquals(new boolean[]{false, true, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_OUTLINE, CAT_FACE_LEFT, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(3);
        sc.processNewMessage(MessageConstants.CURRENT_PLAYERS_HEADER + "NONE");
        assertArrayEquals(new boolean[]{false, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_OUTLINE, CAT_OUTLINE, CAT_BACK_ONLY, CAT_OUTLINE}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(4);
        sc.processNewMessage(MessageConstants.CURRENT_PLAYERS_HEADER + "NONE");
        assertArrayEquals(new boolean[]{false, false, false, true}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE, CAT_FACE_RIGHT}, sc.getDrawnImages());
    }

    @Test
    public void testAdditionalPlayer() {
        testConnection();
        sc.processNewMessage(MessageConstants.NEW_PLAYER_HEADER + 3);
        assertArrayEquals(new boolean[]{true, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_BACK_ONLY, CAT_OUTLINE}, sc.getDrawnImages());
    }

    @Test
    public void testRemovePlayer() {
        testAdditionalPlayer();
        sc.processNewMessage(MessageConstants.DISCONNECT_PLAYER_HEADER + 3);
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
    }


}
