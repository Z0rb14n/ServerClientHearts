package net;

import net.message.client.ClientToServerMessage;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * Represents a client and the message it sent
 */
public final class MessagePair {
    public ModifiedClient modifiedClient;
    public ClientToServerMessage msg;

    /**
     * Initializes the message pair with given client and message
     *
     * @param c   Client that sent the message
     * @param msg Message the client sent
     * @throws IllegalArgumentException if any param is null
     */
    @Contract("_, null -> fail; null, _ -> fail")
    public MessagePair(ModifiedClient c, ClientToServerMessage msg) {
        if (c == null || msg == null) throw new IllegalArgumentException();
        this.modifiedClient = c;
        this.msg = msg;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessagePair)) return false;
        MessagePair lol = (MessagePair) o;
        return this.msg.equals(lol.msg) && modifiedClient.equals(lol.modifiedClient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msg, modifiedClient);
    }
}
