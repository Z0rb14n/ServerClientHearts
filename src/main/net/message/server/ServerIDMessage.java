package net.message.server;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class ServerIDMessage implements ServerToClientMessage {
    private static final long serialVersionUID = 1L;
    private static final String ID_HEADER = "ID: ";
    private final String ID;
    private final int playerNumber;
    private final boolean[] existingPlayers;

    private ServerIDMessage() {
        this("", -1, null);
    }

    public ServerIDMessage(@NotNull String id, int playerNumber, boolean[] existingPlayers) {
        ServerToClientMessage.verifyPlayerNumber(playerNumber);
        if (id.length() < 1 || existingPlayers.length != 4) throw new IllegalArgumentException();
        this.ID = id;
        this.playerNumber = playerNumber;
        this.existingPlayers = existingPlayers;
    }

    public String getID() {
        return ID;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public boolean[] getExistingPlayers() {
        return existingPlayers;
    }

    @Override
    public boolean isValid() {
        return playerNumber >= 1 && playerNumber <= 4 && getID().length() > 0 && existingPlayers.length == 4;
    }

    @Override
    public String toString() {
        return ID_HEADER +
                "ID='" + ID + '\'' +
                ", playerNumber=" + playerNumber +
                ", existingPlayers=" + Arrays.toString(existingPlayers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerIDMessage)) return false;
        ServerIDMessage that = (ServerIDMessage) o;
        return playerNumber == that.playerNumber && Objects.equals(ID, that.ID) && Arrays.equals(existingPlayers, that.existingPlayers);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(ID, playerNumber);
        result = 31 * result + Arrays.hashCode(existingPlayers);
        return result;
    }
}
