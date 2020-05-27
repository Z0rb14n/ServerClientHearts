package util;

import net.ServerToClientMessage;
import processing.core.PImage;
import ui.SCHClient;

import java.util.LinkedList;

import static ui.SCHClient.*;

// Represents the state of the client
public class ClientState {
    public static final int MAX_LENGTH = 100;
    private Deck deck;
    private int playernum;
    private boolean gameStarted = false;
    private final LinkedList<ChatMessage> chatMessages = new LinkedList<>();
    private boolean[] exists = new boolean[4];
    private PImage[] drawnImages = new PImage[4];

    // EFFECTS: initializes clientState with empty deck with invalid player number
    public ClientState() {
        playernum = -1;
        deck = new Deck();
        for (int i = 0; i < 4; i++) {
            drawnImages[i] = CAT_OUTLINE;
        }
    }

    // MODIFIES: this
    // EFFECTS: sets the player number
    public void setPlayerNum(int num) {
        if (num < 1 || num > 4) throw new IllegalArgumentException("lmao what");
        playernum = num;
        exists[num - 1] = true;
        toggleDrawnImage(num, true);
    }

    // MODIFIES: this
    // EFFECTS: sets the drawn image of player number num and whether they are online (i.e. ghost cat or normal)
    private void toggleDrawnImage(int num, boolean exist) {
        if (num < 1 || num > 4) throw new IllegalArgumentException("AAAAAAA");
        if (exist) {
            if (num == 1) {
                drawnImages[0] = CAT_DEFAULT;
            } else if (num == 2) {
                drawnImages[1] = CAT_FACE_LEFT;
            } else if (num == 3) {
                drawnImages[2] = CAT_BACK_ONLY;
            } else {
                drawnImages[3] = CAT_FACE_RIGHT;
            }
        } else {
            drawnImages[num - 1] = CAT_OUTLINE;
        }
    }

    // MODIFIES: this
    // EFFECTS: updates the drawn images of player numbers
    private void updateDrawnImages() {
        drawnImages[0] = exists[0] ? CAT_DEFAULT : CAT_OUTLINE;
        drawnImages[1] = exists[1] ? CAT_FACE_LEFT : CAT_OUTLINE;
        drawnImages[2] = exists[2] ? CAT_BACK_ONLY : CAT_OUTLINE;
        drawnImages[3] = exists[3] ? CAT_FACE_RIGHT : CAT_OUTLINE;
    }

    // EFFECTS: gets the images to draw
    public PImage[] getDrawnImages() {
        return drawnImages;
    }

    // EFFECTS: gets the player number
    public int getPlayerNum() {
        return playernum;
    }

    // EFFECTS: gets a list of all chat messages
    public LinkedList<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    // EFFECTS: gets the current existing players
    public boolean[] getExistingPlayers() {
        return exists;
    }

    public Deck getDeck() {
        return deck;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    // MODIFIES: this
    // EFFECTS: handles incoming messages from server
    public void processNewMessage(ServerToClientMessage msgFromServer) {
        handleNewChatMessage(msgFromServer);
        handlePlayerAdditionMessages(msgFromServer);
        handleGameStartMessages(msgFromServer);
    }

    // MODIFIES: this
    // EFFECTS: handles player chat message
    private void handleNewChatMessage(ServerToClientMessage msg) {
        if (msg.isChatMessage()) {
            ChatMessage cm = new ChatMessage(msg.getChatMessageSender(), msg.getChatMessage());
            if (chatMessages.size() == MAX_LENGTH) {
                chatMessages.removeLast();
            }
            chatMessages.addFirst(cm);
            if (SCHClient.isUsingProcessing()) SCHClient.getClient().addNewMessages(cm.toString());
            //CHAT <digit> : message
        }
    }

    // MODIFIES: this
    // EFFECTS: handles new player addition/removal messages
    private void handlePlayerAdditionMessages(ServerToClientMessage msg) {
        int num;
        if (msg.isIDMessage()) {
            System.arraycopy(msg.getExistingPlayers(), 0, exists, 0, 4);
            exists[msg.getPlayerNumber() - 1] = true; // just to be sure
            updateDrawnImages();
        } else if (msg.isPlayerConnectionMessage()) {
            num = msg.getNewConnectedPlayer();
            exists[num - 1] = true;
            toggleDrawnImage(num, true);
        } else if (msg.isPlayerDisconnectMessage()) {
            num = msg.getDisconnectedPlayerNumber();
            exists[num - 1] = false;
            toggleDrawnImage(num, false);
        }
    }

    // MODIFIES: this
    // EFFECTS: handles gameStarting messages
    private void handleGameStartMessages(ServerToClientMessage msg) {
        if (msg.isGameStartingMessage()) {
            gameStarted = true;
            this.deck = msg.getStartingHand().copy();
        }
    }
}
