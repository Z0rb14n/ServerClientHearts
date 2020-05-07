import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import processing.core.PApplet;
import processing.core.PImage;
import ui.ServerClientHeartsClient;
import util.ClientState;

import static net.ServerToClientMessage.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ui.ServerClientHeartsClient.*;


// DOES NOT WORK!!!!!
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientStateTest {
    ClientState sc;

    @BeforeAll
    public void runBeforeAll() {
        ServerClientHeartsClient sch = new ServerClientHeartsClient();
        PApplet.runSketch(new String[]{"lmao"}/*Processing arguments*/, sch);
    }

    @BeforeEach
    public void runBefore() {
        sc = new ClientState();
    }

    @Test
    public void testConnection() {
        ServerClientHeartsClient schc = new ServerClientHeartsClient();
        sc.setPlayerNum(1);
        sc.processNewMessage(schc, createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
    }

    @Test
    public void testConnectionNonOneStart() {
        ServerClientHeartsClient schc = new ServerClientHeartsClient();
        sc.setPlayerNum(1);
        sc.processNewMessage(schc, createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(2);
        sc.processNewMessage(schc, createIDMessage("LMAO", 2, new boolean[]{false, true, false, false}));
        assertArrayEquals(new boolean[]{false, true, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_OUTLINE, CAT_FACE_LEFT, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(3);
        sc.processNewMessage(schc, createIDMessage("LMAO", 3, new boolean[]{false, false, true, false}));
        assertArrayEquals(new boolean[]{false, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_OUTLINE, CAT_OUTLINE, CAT_BACK_ONLY, CAT_OUTLINE}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(4);
        sc.processNewMessage(schc, createIDMessage("LMAO", 4, new boolean[]{false, false, false, true}));
        assertArrayEquals(new boolean[]{false, false, false, true}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE, CAT_FACE_RIGHT}, sc.getDrawnImages());
    }

    @Test
    public void testAdditionalPlayer() {
        ServerClientHeartsClient schc = new ServerClientHeartsClient();
        sc.setPlayerNum(1);
        sc.processNewMessage(schc, createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
        sc.processNewMessage(schc, createConnectionMessage(3));
        assertArrayEquals(new boolean[]{true, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_BACK_ONLY, CAT_OUTLINE}, sc.getDrawnImages());
    }

    @Test
    public void testRemovePlayer() {
        ServerClientHeartsClient schc = new ServerClientHeartsClient();
        sc.setPlayerNum(1);
        sc.processNewMessage(schc, createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
        sc.processNewMessage(schc, createConnectionMessage(3));
        assertArrayEquals(new boolean[]{true, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_BACK_ONLY, CAT_OUTLINE}, sc.getDrawnImages());
        sc.processNewMessage(schc, createDisconnectMessage(3));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new PImage[]{CAT_DEFAULT, CAT_OUTLINE, CAT_OUTLINE, CAT_OUTLINE}, sc.getDrawnImages());
    }

    @Test
    public void testAddChatMessage() {
        ServerClientHeartsClient schc = new ServerClientHeartsClient();
        sc.processNewMessage(schc, createChatMessage("HI", 4));
        assertEquals(1, sc.getChatMessages().size());
        assertEquals(4, sc.getChatMessages().get(0).playerNumberSender);
        assertEquals("HI", sc.getChatMessages().get(0).message);
        sc.processNewMessage(schc, createChatMessage("HELLO", 2));
        assertEquals(2, sc.getChatMessages().size());
        assertEquals(2, sc.getChatMessages().get(0).playerNumberSender);
        assertEquals("HELLO", sc.getChatMessages().get(0).message);
    }

    @Test
    public void testAddOverMaxCapacity() {
        ServerClientHeartsClient schc = new ServerClientHeartsClient();
        for (int i = 0; i < ClientState.MAX_LENGTH; i++) {
            sc.processNewMessage(schc, createChatMessage("" + i, 3));
        }
        sc.processNewMessage(schc, createChatMessage("" + (ClientState.MAX_LENGTH + 1), 3));
        assertEquals(3, sc.getChatMessages().getLast().playerNumberSender);
        assertEquals("1", sc.getChatMessages().getLast().message);
    }
}
