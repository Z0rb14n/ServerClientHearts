package util;

// Represents a chat message
public class ChatMessage {
    public int playerNumberSender;
    public String message;

    // EFFECTS: initializes the chat message with given player number and message
    ChatMessage(int number, String msg) {
        message = msg;
        playerNumberSender = number;
    }

    @Override
    // EFFECTS: returns string representation of the message
    public String toString() {
        return "Player " + playerNumberSender + ": " + message;
    }
}
