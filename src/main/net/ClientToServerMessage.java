package net;

import util.Card;
import util.Deck;

import java.io.Serializable;

// Represents a message sent from a client to the server
public final class ClientToServerMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String INVALID_MSG = "INVALID MSG";
    private static final String CHAT_MSG_HEADER = "CHAT MSG:";
    private static final String FIRST_THREE_HEADER = "FIRST THREE:";
    private static final String NEW_CARD_HEADER = "NEW CARD:";

    private boolean isChatMessage = false;
    private String chatMessage = ""; // send new messages

    // EFFECTS: blank constructor - produces invalid message (hence a private constructor)
    private ClientToServerMessage() {
    }

    // EFFECTS: determines whether this message is valid
    public boolean isValidMessage() {
        return isChatMessage() || isFirstThreeCardsMessage() || isNewCardPlayedMessage();
    }

    // EFFECTS: creates a new ClientToServerMessage in the format of a chat message
    static ClientToServerMessage createNewChatMessage(String msgContents) {
        final ClientToServerMessage csm = new ClientToServerMessage();
        csm.isChatMessage = true;
        csm.chatMessage = msgContents;
        csm.cards.clear();
        csm.card = null;
        return csm;
    }

    // EFFECTS: determines whether this message is a chat message
    boolean isChatMessage() {
        return isChatMessage;
    }

    // EFFECTS: returns the chat message;
    String getChatMessage() {
        if (!isChatMessage) throw new IllegalArgumentException();
        return chatMessage;
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

    // EFFECTS: returns the three cards to pass onto the next player
    public Deck getThreeCards() {
        return cards;
    }

    private Card card;

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

    // EFFECTS: returns the singular card played
    public Card getCard() {
        return card;
    }

    @Override
    // EFFECTS: returns the string representation of this message
    public String toString() {
        if (isChatMessage()) return CHAT_MSG_HEADER + getChatMessage();
        if (isFirstThreeCardsMessage()) return FIRST_THREE_HEADER + cards.toString();
        if (isNewCardPlayedMessage()) return NEW_CARD_HEADER + card.toString();
        return INVALID_MSG;
    }

    @Override
    // EFFECTS: determines if two objects are equal
    public boolean equals(Object o) {
        if (!(o instanceof ClientToServerMessage)) return false;
        ClientToServerMessage newO = (ClientToServerMessage) o;
        if (newO.isChatMessage()) return isChatMessage() && newO.getChatMessage().equals(getChatMessage());
        if (newO.isFirstThreeCardsMessage()) return isFirstThreeCardsMessage() && cards.equals(newO.cards);
        if (newO.isNewCardPlayedMessage()) return isNewCardPlayedMessage() && card.equals(newO.card);
        return isValidMessage() == newO.isValidMessage();
    }
}
