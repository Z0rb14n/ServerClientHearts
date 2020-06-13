package net;

import processing.net.Client;

// Represents a client and the message it sent
public final class MessagePair {
    public Client client;
    public ClientToServerMessage msg;

    // EFFECTS: initializes the message pair with given client and message
    public MessagePair(Client c, ClientToServerMessage msg) {
        this.client = c;
        this.msg = msg;
    }

    @Override
    // EFFECTS: determines if the two objects are equal
    public boolean equals(Object o) {
        if (!(o instanceof MessagePair)) return false;
        MessagePair lol = (MessagePair) o;
        return this.client.equals(lol.client) && this.msg.equals(lol.msg);
    }

    @Override
    // EFFECTS: gets the hash code of this object
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + client.hashCode();
        hash = 31 * hash + msg.hashCode();
        return hash;
    }
}
