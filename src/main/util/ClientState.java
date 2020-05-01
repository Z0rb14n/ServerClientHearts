package util;

import net.MessageConstants;
import processing.core.PImage;

import java.util.ArrayList;

import static ui.ServerClientHeartsClient.*;

public class ClientState {
    private static final int MAX_LENGTH = 100;
    private static final String CHAT_MSG_FORMAT = MessageConstants.OUTGOING_CHAT_MSG;
    Deck deck;
    private int playernum;
    private final ArrayList<ChatMessage> chatMessages = new ArrayList<>(100);
    boolean[] exists = new boolean[4];
    PImage[] drawnImages = new PImage[4];
    public ClientState() {
        playernum = -1;
        deck = new Deck();
        for (int i = 0; i < 4; i++) {
            drawnImages[i] = CAT_OUTLINE;
        }
    }

    public void setPlayerNum(int num) {
        if (num < 1 || num > 4) throw new IllegalArgumentException("lmao what");
        playernum = num;
        exists[num - 1] = true;
        toggleDrawnImage(num, true);
    }

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

    public PImage[] getDrawnImages() {
        return drawnImages;
    }

    public int getPlayerNum() {
        return playernum;
    }

    public ArrayList<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public boolean[] getExistingPlayers() {
        return exists;
    }

    // MODIFIES: this
    // EFFECTS: handles incoming messages from server
    public void processNewMessage(String msgFromServer) {
        if (msgFromServer.matches(CHAT_MSG_FORMAT)) {
            assert ((msgFromServer.charAt(4) + "").matches("\\d"));
            ChatMessage cm = new ChatMessage(msgFromServer.charAt(4) - '0', msgFromServer.substring(6));
            if (chatMessages.size() == MAX_LENGTH) {
                chatMessages.remove(0);
            }
            chatMessages.add(cm);
            //CHAT <digit> : message
            return;
        }
        handlePlayerAdditionMessages(msgFromServer);
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
                        throw new IllegalArgumentException("AAAAAAAAA");
                    }
                    num = Character.digit(msg.charAt(startingIndex), 10) - 1;
                    exists[num] = true;
                    toggleDrawnImage(num, true);
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
