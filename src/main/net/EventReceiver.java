package net;

public interface EventReceiver {
    // MODIFIES: this
    // EFFECTS: runs when a client connects to the server
    default void serverEvent(ModifiedServer s, ModifiedClient c) {
    }

    default void clientEvent(ModifiedClient c) {
    }

    default void disconnectEvent(ModifiedClient c) {
    }
}
