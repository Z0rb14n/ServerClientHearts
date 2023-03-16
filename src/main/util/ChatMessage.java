package util;

// Represents a chat message

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a chat message
 */
public class ChatMessage {
    public final int playerNumberSender;
    @NotNull
    public final String message;

    /**
     * Initializes the chat message with given player number/sender and string message
     *
     * @param number Player number of sender (number between 1-4)
     * @param msg    Chat message body
     * @throws IllegalArgumentException if number is out of bounds (i.e. not [1-4])
     */
    public ChatMessage(int number, @NotNull String msg) {
        if (number < 0 || number > 4) throw new IllegalArgumentException();
        message = msg;
        playerNumberSender = number;
    }

    @Override
    @Contract(pure = true)
    public String toString() {
        if (playerNumberSender == 0) return "[Server] " + message;
        return "Player " + playerNumberSender + ": " + message;
    }
}
