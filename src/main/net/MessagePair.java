package net;

import processing.net.Client;

public final class MessagePair {
    public Client client;
    public ClientToServerMessage msg;

    protected MessagePair() {
        client = null;
        msg = null;
    }

    public MessagePair(Client c, ClientToServerMessage msg) {
        this.client = c;
        this.msg = msg;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessagePair)) return false;
        MessagePair lol = (MessagePair) o;
        return this.client.equals(lol.client) && this.msg.equals(lol.msg);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + client.hashCode();
        hash = 31 * hash + msg.hashCode();
        return hash;
    }
}
