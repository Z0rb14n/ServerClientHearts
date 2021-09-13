package net.message.server;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ServerChatMessage implements ServerToClientMessage {
    private static final long serialVersionUID = 1L;
    private static final String CHAT_HEADER = "CHAT: "; // "CHAT:\\d,.*"
    @NotNull
    private final String message;
    private final int playerNumber;

    public ServerChatMessage() {
        this("", -1);
    }

    public ServerChatMessage(@NotNull String message, int playerNumber) {
        ServerToClientMessage.verifyPlayerNumber(playerNumber);
        this.message = message;
        this.playerNumber = playerNumber;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean isValid() {
        return playerNumber >= 1 && playerNumber <= 4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerChatMessage)) return false;
        ServerChatMessage that = (ServerChatMessage) o;
        return playerNumber == that.playerNumber && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, playerNumber);
    }

    @Override
    public String toString() {
        return CHAT_HEADER + "message='" + message + '\'' +
                ", playerNumber=" + playerNumber;
    }
}
