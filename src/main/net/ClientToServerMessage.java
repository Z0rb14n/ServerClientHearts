package net;

import util.Card;
import util.Deck;

import java.io.Serializable;

// Represents a message sent from a client to the server
public final class ClientToServerMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isChatMessage = false;
    private String chatMessage = ""; // send new messages

    // EFFECTS: blank constructor - produces invalid message (hence a private constructor)
    private ClientToServerMessage() {
    }

    // EFFECTS: determines whether this message is valid
    public boolean isValidMessage() {
        return !isChatMessage() && !isFirstThreeCardsMessage() && !isNewCardPlayedMessage();
    }

    // EFFECTS: creates a new ClientToServerMessage in the format of a chat message
    public static ClientToServerMessage createNewChatMessage(String msgContents) {
        final ClientToServerMessage csm = new ClientToServerMessage();
        csm.isChatMessage = true;
        csm.chatMessage = msgContents;
        csm.cards.clear();
        csm.card = null;
        return csm;
    }

    // EFFECTS: determines whether this message is a chat message
    public boolean isChatMessage() {
        return isChatMessage;
    }

    private boolean isFirstThreeCards = false;
    private Deck cards = new Deck(); // send first three cards

    // EFFECTS: creates a new ClientToServerMessage in the format of passing 3 cards message
    public static ClientToServerMessage createNewSubmitThreeCardMessage(Deck cards) {
        if (cards.deckSize() != 3) throw new IllegalArgumentException("Invalid number of cards.");
        final ClientToServerMessage csm = new ClientToServerMessage();
        csm.isFirstThreeCards = true;
        csm.cards.addAll(cards);
        csm.card = null;
        return csm;
    }

    // EFFECTS: creates a new ClientToServerMessage in the format of passing 3 cards message
    public static ClientToServerMessage createNewSubmitThreeCardMessage(Card c1, Card c2, Card c3) {
        if (c1 == null || c2 == null || c3 == null) throw new IllegalArgumentException("Cannot add null cards");
        final ClientToServerMessage csm = new ClientToServerMessage();
        csm.isFirstThreeCards = true;
        csm.cards.addCard(c1.copy());
        csm.cards.addCard(c2.copy());
        csm.cards.addCard(c3.copy());
        csm.card = null;
        return csm;
    }

    // EFFECTS: determines whether the card is a passing 3 cards message
    public boolean isFirstThreeCardsMessage() {
        return isFirstThreeCards;
    }

    private Card card; // future cards played

    // EFFECTS: creates a new ClientToServerMessage in the format of a card played message
    public static ClientToServerMessage createNewCardPlayedMessage(Card c) {
        if (c == null) throw new IllegalArgumentException("Cannot add null card.");
        final ClientToServerMessage csm = new ClientToServerMessage();
        csm.card = c;
        return csm;
    }

    // EFFECTS: determines whether this message is a card-played message
    public boolean isNewCardPlayedMessage() {
        return card != null;
    }
}
