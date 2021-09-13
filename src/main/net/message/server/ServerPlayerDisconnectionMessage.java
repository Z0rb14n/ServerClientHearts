package net.message.server;

import java.util.Objects;

/**
 * Represents a message that gets sent by the server upon a player disconnecting
 */
public class ServerPlayerDisconnectionMessage implements ServerToClientMessage {
    private static final long serialVersionUID = 1L;
    private static final String PLAYER_DISCONNECT_HEADER = "DISCONNECT:";
    private final int playerNumber;

    private ServerPlayerDisconnectionMessage() {
        this(-1);
    }

    public ServerPlayerDisconnectionMessage(int newPlayerNumber) {
        ServerToClientMessage.verifyPlayerNumber(newPlayerNumber);
        this.playerNumber = newPlayerNumber;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    @Override
    public boolean isValid() {
        return playerNumber >= 1 && playerNumber <= 4;
    }

    @Override
    public String toString() {
        return PLAYER_DISCONNECT_HEADER + playerNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerPlayerDisconnectionMessage)) return false;
        ServerPlayerDisconnectionMessage that = (ServerPlayerDisconnectionMessage) o;
        return playerNumber == that.playerNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerNumber);
    }
}
