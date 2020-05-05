package net;

import util.Deck;

import java.io.Serializable;

public class ServerToClientMessage implements Serializable {
    Deck center;
    Deck urHand;
    String interestingMessage;
}
