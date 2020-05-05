package net;

import util.Card;
import util.Deck;

import java.io.Serializable;

public class ClientToServerMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // in order of which it'll likely be used
    boolean isChatMessage = false;
    String chatMessage = ""; // send new messages

    Deck cards = new Deck(); // send first three cards

    Card card; // future cards played
}
