package net.message.server;

import java.util.Objects;

public class ServerKickMessage implements ServerToClientMessage {
    private static final long serialVersionUID = 1L;
    private static final String KICK_HEADER = "KICK: ";
    private final String message;

    private ServerKickMessage() {
        this("");
    }

    public ServerKickMessage(String message) {
        if (message == null) message = "";
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerKickMessage)) return false;
        ServerKickMessage that = (ServerKickMessage) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return KICK_HEADER + message;
    }
}
