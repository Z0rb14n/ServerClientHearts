package net;

import processing.net.Client;

public class MessagePair {
    public Client client;
    public String message;

    protected MessagePair() {
        client = null;
        message = null;
    }

    public MessagePair(Client c, String message) {
        this.client = c;
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessagePair)) return false;
        MessagePair lol = (MessagePair) o;
        return lol.client.equals(client) && lol.message.equals(message);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + client.hashCode();
        hash = 31 * hash + message.hashCode();
        return hash;
    }
}
