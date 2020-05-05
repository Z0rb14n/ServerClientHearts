package net;

import util.Card;
import util.Deck;

import java.io.Serializable;

public class ClientToServerMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // in order of which it'll likely be used
    private boolean isChatMessage = false;
    private String chatMessage = ""; // send new messages

    private Deck cards = new Deck(); // send first three cards

    private Card card; // future cards played

    private ClientToServerMessage() {
    }

    public static ClientToServerMessage createNewChatMessage(String msgContents) {
        final ClientToServerMessage csm = new ClientToServerMessage();
        csm.isChatMessage = true;
        csm.chatMessage = msgContents;
        csm.cards.clear();
        csm.card = null;
        return csm;
    }

    public static ClientToServerMessage createNewSubmitThreeCardMessage(Deck cards) {
        if (cards.deckSize() != 3) throw new IllegalArgumentException("Invalid number of cards.");
        final ClientToServerMessage csm = new ClientToServerMessage();
        csm.cards.addAll(cards);
        csm.card = null;
        return csm;
    }

    public static ClientToServerMessage createNewSubmitThreeCardMessage(Card c1, Card c2, Card c3) {
        if (c1 == null || c2 == null || c3 == null) throw new IllegalArgumentException("Cannot add null cards");
        final ClientToServerMessage csm = new ClientToServerMessage();
        csm.cards.addCard(c1.copy());
        csm.cards.addCard(c2.copy());
        csm.cards.addCard(c3.copy());
        csm.card = null;
        return csm;
    }

    public static ClientToServerMessage createNewCardPlayedMessage(Card c) {
        if (c == null) throw new IllegalArgumentException("Cannot add null card.");
        final ClientToServerMessage csm = new ClientToServerMessage();
        csm.card = c;
        return csm;
    }
}
