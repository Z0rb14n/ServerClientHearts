package net;

// Represents a receiver for Server/Client events
public interface EventReceiver {
    // MODIFIES: this
    // EFFECTS: runs when a client connects to the server
    //          called by ModifiedClient
    default void serverEvent(ModifiedServer s, ModifiedClient c) {
    }

    // MODIFIES: this
    // EFFECTS: runs when a client has received data from the server
    //          called by ModifiedClient
    default void clientEvent(ModifiedClient c) {
    }

    // MODIFIES: this
    // EFFECTS: runs when a client disconnects from the server
    //          called by ModifiedServer
    default void disconnectEvent(ModifiedClient c) {
    }
}
