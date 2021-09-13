package net.message.server;

public class ServerGameResetMessage implements ServerToClientMessage {
    private static final String RESET_MSG = "RESET";

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return RESET_MSG;
    }
}
