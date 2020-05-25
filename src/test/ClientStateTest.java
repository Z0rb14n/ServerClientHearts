import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import processing.core.PApplet;
import processing.core.PImage;
import ui.SCHClient;
import util.ClientState;

import static net.ServerToClientMessage.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ui.SCHClient.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientStateTest {
    ClientState sc;

    @BeforeAll
    public void runBeforeAll() {
        SCHClient schc = SCHClient.getClient();
        PApplet.runSketch(new String[]{"lmao"}/*Processing arguments*/, schc);
    }

    @BeforeEach
    public void runBefore() {
        sc = new ClientState();
    }

    @Test
    public void testConnection() {
        sc.setPlayerNum(1);
        sc.processNewMessage(createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
    }

    @Test
    public void testConnectionNonOneStart() {
        SCHClient.getClient().settings();
        SCHClient.getClient().setup();
        sc.setPlayerNum(1);
        sc.processNewMessage(createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(2);
        sc.processNewMessage(createIDMessage("LMAO", 2, new boolean[]{false, true, false, false}));
        assertArrayEquals(new boolean[]{false, true, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_OUTLINE, CAT_FACE_LEFT, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(3);
        sc.processNewMessage(createIDMessage("LMAO", 3, new boolean[]{false, false, true, false}));
        assertArrayEquals(new boolean[]{false, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_OUTLINE, CAT_OUTLINE, CAT_BACK_ONLY, CAT_OUTLINE}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(4);
        sc.processNewMessage(createIDMessage("LMAO", 4, new boolean[]{false, false, false, true}));
        assertArrayEquals(new boolean[]{false, false, false, true}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE, CAT_FACE_RIGHT}, sc.getDrawnImages());
    }

    @Test
    public void testAdditionalPlayer() {
        sc.setPlayerNum(1);
        sc.processNewMessage(createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
        sc.processNewMessage(createConnectionMessage(3));
        assertArrayEquals(new boolean[]{true, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_BACK_ONLY, CAT_OUTLINE}, sc.getDrawnImages());
    }

    @Test
    public void testRemovePlayer() {
        sc.setPlayerNum(1);
        sc.processNewMessage(createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
        sc.processNewMessage(createConnectionMessage(3));
        assertArrayEquals(new boolean[]{true, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_BACK_ONLY, CAT_OUTLINE}, sc.getDrawnImages());
        sc.processNewMessage(createDisconnectMessage(3));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
    }

    @Test
    // Ignore since it makes calls to processing
    public void testAddChatMessage() {
        sc.processNewMessage(createChatMessage("HI", 4));
        assertEquals(1, sc.getChatMessages().size());
        assertEquals(4, sc.getChatMessages().get(0).playerNumberSender);
        assertEquals("HI", sc.getChatMessages().get(0).message);
        sc.processNewMessage(createChatMessage("HELLO", 2));
        assertEquals(2, sc.getChatMessages().size());
        assertEquals(2, sc.getChatMessages().get(0).playerNumberSender);
        assertEquals("HELLO", sc.getChatMessages().get(0).message);
    }

    @Test
    // Ignore since it makes calls to processing
    public void testAddOverMaxCapacity() {
        for (int i = 0; i < ClientState.MAX_LENGTH; i++) {
            sc.processNewMessage(createChatMessage("" + i, 3));
        }
        sc.processNewMessage(createChatMessage("" + (ClientState.MAX_LENGTH + 1), 3));
        assertEquals(3, sc.getChatMessages().getLast().playerNumberSender);
        assertEquals("1", sc.getChatMessages().getLast().message);
    }
}
