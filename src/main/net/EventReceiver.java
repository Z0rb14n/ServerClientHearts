package net;

// Represents a receiver for Server/Client events
public interface EventReceiver {
    // MODIFIES: this
    // EFFECTS: runs when a client connects to the server
    //          called by ModifiedServer
    default void clientConnectionEvent(ModifiedServer s, ModifiedClient c) {
    }

    // MODIFIES: this
    // EFFECTS: runs when a client has received data from the server
    //          called by ModifiedClient
    default void dataReceivedEvent(ModifiedClient c) {
    }

    // MODIFIES: this
    // EFFECTS: runs when a client disconnects from the server
    //          called by ModifiedClient
    default void disconnectEvent(ModifiedClient c) {
    }

    // MODIFIES: this
    // EFFECTS: runs when a client encounters end-of-stream (e.g. host disconnect, kick)
    default void endOfStreamEvent(ModifiedClient c) {
    }
}
