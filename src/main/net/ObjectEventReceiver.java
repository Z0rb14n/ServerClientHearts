package net;

public interface ObjectEventReceiver extends EventReceiver {
    /**
     * Runs when a client has received data (parsed as an object) from the server.
     * <p>
     * Called by modified client.
     *
     * @param c ModifiedClient that received data.
     * @param o Object received by the client.
     */
    default void dataReceivedEvent(ModifiedClient c, Object o) {
    }
}
