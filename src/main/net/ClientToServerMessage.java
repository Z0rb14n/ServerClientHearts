package net;

import util.Deck;

import java.io.Serializable;

public class ClientToServerMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    Deck cards;
    String interestingMessage;
}
