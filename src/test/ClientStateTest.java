import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ui.client.PlayerView;
import util.ClientState;

import java.awt.image.BufferedImage;

import static net.ServerToClientMessage.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO FINISH TESTS

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientStateTest {
    private ClientState sc;
    private BufferedImage defaultCat;
    private BufferedImage outline;
    private BufferedImage left;
    private BufferedImage right;
    private BufferedImage back;
    @BeforeAll
    void runBeforeAll() {
        PlayerView.initCats();
        defaultCat = PlayerView.getCatDefault();
        assert defaultCat != null;
        outline = PlayerView.getOutlineCat();
        assert outline != null;
        left = PlayerView.getCatFaceLeft();
        assert left != null;
        right = PlayerView.getCatFaceRight();
        assert right != null;
        back = PlayerView.getCatBackOnly();
        assert back != null;
    }

    @BeforeEach
    void runBefore() {
        sc = new ClientState();
    }

    @Test
    void testConnection() {
        sc.setPlayerNum(1);
        sc.processNewMessage(createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new BufferedImage[]{defaultCat, outline, outline, outline}, sc.getDrawnImages());
    }

    @Test
    void testConnectionNonOneStart() {
        sc.setPlayerNum(1);
        sc.processNewMessage(createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new BufferedImage[]{defaultCat, outline, outline, outline}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(2);
        sc.processNewMessage(createIDMessage("LMAO", 2, new boolean[]{false, true, false, false}));
        assertArrayEquals(new boolean[]{false, true, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new BufferedImage[]{outline, left, outline, outline}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(3);
        sc.processNewMessage(createIDMessage("LMAO", 3, new boolean[]{false, false, true, false}));
        assertArrayEquals(new boolean[]{false, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new BufferedImage[]{outline, outline, back, outline}, sc.getDrawnImages());
        runBefore();
        sc.setPlayerNum(4);
        sc.processNewMessage(createIDMessage("LMAO", 4, new boolean[]{false, false, false, true}));
        assertArrayEquals(new boolean[]{false, false, false, true}, sc.getExistingPlayers());
        assertArrayEquals(new BufferedImage[]{outline, outline, outline, right}, sc.getDrawnImages());
    }

    @Test
    void testAdditionalPlayer() {
        sc.setPlayerNum(1);
        sc.processNewMessage(createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new BufferedImage[]{defaultCat, outline, outline, outline}, sc.getDrawnImages());
        sc.processNewMessage(createConnectionMessage(3));
        assertArrayEquals(new boolean[]{true, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new BufferedImage[]{defaultCat, outline, back, outline}, sc.getDrawnImages());
    }

    @Test
    void testRemovePlayer() {
        sc.setPlayerNum(1);
        sc.processNewMessage(createIDMessage("LMAO", 1, new boolean[]{true, false, false, false}));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new BufferedImage[]{defaultCat, outline, outline, outline}, sc.getDrawnImages());
        sc.processNewMessage(createConnectionMessage(3));
        assertArrayEquals(new boolean[]{true, false, true, false}, sc.getExistingPlayers());
        assertArrayEquals(new BufferedImage[]{defaultCat, outline, back, outline}, sc.getDrawnImages());
        sc.processNewMessage(createDisconnectMessage(3));
        assertArrayEquals(new boolean[]{true, false, false, false}, sc.getExistingPlayers());
        assertArrayEquals(new BufferedImage[]{defaultCat, outline, outline, outline}, sc.getDrawnImages());
    }

    @Test
    void testAddChatMessage() {
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
    void testAddOverMaxCapacity() {
        for (int i = 0; i < ClientState.MAX_LENGTH; i++) {
            sc.processNewMessage(createChatMessage("" + i, 3));
        }
        sc.processNewMessage(createChatMessage("" + (ClientState.MAX_LENGTH + 1), 3));
        assertEquals(3, sc.getChatMessages().getLast().playerNumberSender);
        assertEquals("1", sc.getChatMessages().getLast().message);
    }
}
