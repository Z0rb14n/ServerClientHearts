package util;

// Represents a chat message
public class ChatMessage {
    public int playerNumberSender;
    public String message;

    // EFFECTS: initializes the chat message with given player number and message
    //          throws IllegalArgumentException if number is out of bounds (i.e. not 1-4)
    ChatMessage(int number, String msg) {
        if (number < 1 || number > 4) throw new IllegalArgumentException();
        message = msg;
        playerNumberSender = number;
    }

    @Override
    // EFFECTS: returns string representation of the message
    public String toString() {
        return "Player " + playerNumberSender + ": " + message;
    }
}
