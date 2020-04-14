package util;

import ui.ServerClientHearts;

import java.util.ArrayList;

public class ClientState {
    private static final int MAX_LENGTH = 100;
    private static final String CHAT_MSG_FORMAT = ServerClientHearts.OUTGOING_CHAT_MSG;
    Deck deck;
    private int playernum;
    private final ArrayList<ChatMessage> chatMessages = new ArrayList<>(100);

    public ClientState() {
        playernum = -1;
        deck = new Deck();
    }

    public void setPlayerNum(int num) {
        playernum = num;
    }

    public int getPlayerNum() {
        return playernum;
    }

    public ArrayList<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    // MODIFIES: this
    // EFFECTS: adds a new message to chatMessages
    public void processNewMessage(String msgFromServer) {
        if (msgFromServer.matches(CHAT_MSG_FORMAT)) {
            assert ((msgFromServer.charAt(4) + "").matches("\\d"));
            ChatMessage cm = new ChatMessage(msgFromServer.charAt(4) - '0', msgFromServer.substring(6));
            if (chatMessages.size() == MAX_LENGTH) {
                chatMessages.remove(0);
            }
            chatMessages.add(cm);
            //CHAT <digit> : message
        }
    }
}
