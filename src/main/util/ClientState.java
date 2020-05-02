package util;

import net.MessageConstants;
import processing.core.PImage;
import ui.ServerClientHeartsClient;

import java.util.LinkedList;

import static ui.ServerClientHeartsClient.*;

// Represents the state of the client
public class ClientState {
    public static final int MAX_LENGTH = 100;
    private static final String CHAT_MSG_FORMAT = MessageConstants.OUTGOING_CHAT_MSG;
    private Deck deck;
    private int playernum;
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

    // MODIFIES: this
    // EFFECTS: handles incoming messages from server
    public void processNewMessage(ServerClientHeartsClient caller, String msgFromServer) {
        handleNewChatMessage(caller, msgFromServer);
        handlePlayerAdditionMessages(msgFromServer);
    }

    // MODIFIES: this
    // EFFECTS: handles player chat message
    private void handleNewChatMessage(ServerClientHeartsClient caller, String msg) {
        if (msg.matches(CHAT_MSG_FORMAT)) {
            ChatMessage cm = new ChatMessage(msg.charAt(4) - '0', msg.substring(6));
            if (chatMessages.size() == MAX_LENGTH) {
                chatMessages.removeLast();
            }
            chatMessages.addFirst(cm);
            caller.addNewMessages("Player " + cm.playerNumberSender + ": " + cm.message);
            //CHAT <digit> : message
        }
    }

    // MODIFIES: this
    // EFFECTS: handles new player addition/removal messages
    private void handlePlayerAdditionMessages(String msg) {
        int num;
        if (msg.startsWith(MessageConstants.CURRENT_PLAYERS_HEADER)) {
            if (!msg.equals(MessageConstants.CURRENT_PLAYERS_HEADER + "NONE")) {
                int startingIndex = MessageConstants.CURRENT_PLAYERS_HEADER.length();
                for (; startingIndex < msg.length(); startingIndex++) {
                    if (!Character.isDigit(msg.charAt(startingIndex))) {
                        continue;
                    }
                    num = Character.digit(msg.charAt(startingIndex), 10) - 1;
                    exists[num] = true;
                    toggleDrawnImage(num + 1, true);
                }
            }
        } else if (msg.startsWith(MessageConstants.NEW_PLAYER_HEADER)) {
            num = Character.digit(msg.charAt(MessageConstants.NEW_PLAYER_HEADER.length()), 10) - 1;
            exists[num] = true;
            toggleDrawnImage(num + 1, true);
        } else if (msg.startsWith(MessageConstants.DISCONNECT_PLAYER_HEADER)) {
            num = Character.digit(msg.charAt(MessageConstants.NEW_PLAYER_HEADER.length()), 10) - 1;
            exists[num] = false;
            toggleDrawnImage(num + 1, false);
        }
    }
}
