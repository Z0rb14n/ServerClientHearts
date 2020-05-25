package util;

public class ChatMessage {
    public int playerNumberSender;
    public String message;

    ChatMessage(int number, String msg) {
        message = msg;
        playerNumberSender = number;
    }
}
