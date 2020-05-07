package net;

import processing.net.Client;

public final class MessagePair {
    public Client client;
    public String message;
    public ClientToServerMessage msg;

    protected MessagePair() {
        client = null;
        message = null;
    }

    public MessagePair(Client c, String message) {
        this.client = c;
        this.message = message;
    }

    public MessagePair(Client c, ClientToServerMessage msg) {
        this.client = c;
        this.msg = msg;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessagePair)) return false;
        MessagePair lol = (MessagePair) o;
        if (message == null) {
            return lol.message == null && this.msg.equals(lol.msg);
        } else {
            return lol.msg == this.msg && message.equals(lol.message);
        }
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + client.hashCode();
        hash = 31 * hash + message.hashCode();
        hash = 31 * hash + msg.hashCode();
        return hash;
    }
}
