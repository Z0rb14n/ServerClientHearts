package util;

public class ChatMessage {
    public int playerNumberSender;
    public String message;

    ChatMessage(int number, String msg) {
        message = msg;
        playerNumberSender = number;
    }

    @Override
    public String toString() {
        return "Player " + playerNumberSender + ": " + message;
    }
}
