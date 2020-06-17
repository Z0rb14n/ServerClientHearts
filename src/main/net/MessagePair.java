package net;

import processing.net.Client;

import java.util.Objects;

// Represents a client and the message it sent
public final class MessagePair {
    public Client client;
    public ModifiedClient modifiedClient;
    public ClientToServerMessage msg;

    // EFFECTS: initializes the message pair with given client and message
    public MessagePair(Client c, ClientToServerMessage msg) {
        this.client = c;
        this.msg = msg;
    }

    // EFFECTS: initializes the message pair with given client and message
    public MessagePair(ModifiedClient c, ClientToServerMessage msg) {
        this.modifiedClient = c;
        this.msg = msg;
    }

    @Override
    // EFFECTS: determines if the two objects are equal
    public boolean equals(Object o) {
        if (!(o instanceof MessagePair)) return false;
        MessagePair lol = (MessagePair) o;
        return this.msg.equals(lol.msg) && Objects.equals(client, lol.client) && Objects.equals(modifiedClient, lol.modifiedClient);
    }

    @Override
    // EFFECTS: gets the hash code of this object
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + msg.hashCode();
        hash = 31 * hash + Objects.hashCode(modifiedClient);
        hash = 31 * hash + Objects.hashCode(client);
        return hash;
    }
}
