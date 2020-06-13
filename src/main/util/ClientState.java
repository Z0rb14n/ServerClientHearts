package util;

import net.ServerToClientMessage;
import ui.client.PlayerView;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

// Represents the state of the client
public class ClientState {
    public static final int MAX_LENGTH = 100;
    private Deck deck;
    private int playernum;
    private boolean gameStarted = false;
    private final LinkedList<ChatMessage> chatMessages = new LinkedList<>();
    private boolean[] exists = new boolean[4];
    private BufferedImage[] drawnImages = new BufferedImage[4];


    // EFFECTS: initializes clientState with empty deck with invalid player number
    public ClientState() {
        playernum = -1;
        deck = new Deck();
        for (int i = 0; i < 4; i++) {
            drawnImages[i] = PlayerView.getOutlineCat();
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
                drawnImages[0] = PlayerView.getCatDefault();
            } else if (num == 2) {
                drawnImages[1] = PlayerView.getCatFaceLeft();
            } else if (num == 3) {
                drawnImages[2] = PlayerView.getCatBackOnly();
            } else {
                drawnImages[3] = PlayerView.getCatFaceRight();
            }
        } else {
            drawnImages[num - 1] = PlayerView.getOutlineCat();
        }
    }

    // MODIFIES: this
    // EFFECTS: updates the drawn images of player numbers
    private void updateDrawnImages() {
        drawnImages[0] = exists[0] ? PlayerView.getCatDefault() : PlayerView.getOutlineCat();
        drawnImages[1] = exists[1] ? PlayerView.getCatFaceLeft() : PlayerView.getOutlineCat();
        drawnImages[2] = exists[2] ? PlayerView.getCatBackOnly() : PlayerView.getOutlineCat();
        drawnImages[3] = exists[3] ? PlayerView.getCatFaceRight() : PlayerView.getOutlineCat();
    }

    // EFFECTS: gets the images to draw
    public BufferedImage[] getDrawnImages() {
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

    // EFFECTS: gets the client's deck
    public Deck getDeck() {
        return deck;
    }

    // EFFECTS: determines if the game has started
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
        }
    }

    // MODIFIES: this
    // EFFECTS: handles new player addition/removal messages (includes ID message)
    private void handlePlayerAdditionMessages(ServerToClientMessage msg) {
        int num;
        if (msg.isIDMessage()) {
            System.arraycopy(msg.getExistingPlayers(), 0, exists, 0, 4);
            playernum = msg.getPlayerNumber();
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
