package net.message.server;

import java.util.Objects;

/**
 * Represents a message that gets sent by the server upon a new player connecting
 */
public class ServerPlayerConnectionMessage implements ServerToClientMessage {
    private static final long serialVersionUID = 1L;
    private static final String PLAYER_CONNECT_HEADER = "CONNECT: ";
    private final int newPlayerNumber;

    private ServerPlayerConnectionMessage() {
        this(-1);
    }

    public ServerPlayerConnectionMessage(int newPlayerNumber) {
        ServerToClientMessage.verifyPlayerNumber(newPlayerNumber);
        this.newPlayerNumber = newPlayerNumber;
    }

    public int getNewPlayerNumber() {
        return newPlayerNumber;
    }

    @Override
    public boolean isValid() {
        return newPlayerNumber >= 1 && newPlayerNumber <= 4;
    }

    @Override
    public String toString() {
        return PLAYER_CONNECT_HEADER + newPlayerNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerPlayerConnectionMessage)) return false;
        ServerPlayerConnectionMessage that = (ServerPlayerConnectionMessage) o;
        return newPlayerNumber == that.newPlayerNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newPlayerNumber);
    }
}
