package net.message.client;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import util.card.Card;
import util.card.Deck;

import java.util.Objects;

public class ClientThreeCardMessage implements ClientToServerMessage {
    private static final long serialVersionUID = 1L;
    private static final String FIRST_THREE_HEADER = "THREE CARDS: ";
    @NotNull
    private final Deck cards = new Deck();

    private ClientThreeCardMessage() {
    }

    public ClientThreeCardMessage(@NotNull Card c1, @NotNull Card c2, @NotNull Card c3) {
        cards.add(c1);
        cards.add(c2);
        cards.add(c3);
    }

    /**
     * Creates a ClientThreeCardMessage with given deck to copy from
     *
     * @param deck Deck to create cards from
     * @throws IllegalArgumentException If three cards are not provided
     */
    public ClientThreeCardMessage(Deck deck) {
        if (deck.size() != 3) throw new IllegalArgumentException("Invalid number of cards: " + deck.size());
        cards.add(deck);
    }

    public @NotNull Deck getCards() {
        return cards;
    }

    @Override
    public @NotNull String toString() {
        return FIRST_THREE_HEADER + cards;
    }

    @Override
    public boolean isValid() {
        return cards.size() == 3;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientThreeCardMessage)) return false;
        ClientThreeCardMessage that = (ClientThreeCardMessage) o;
        return cards.equals(that.cards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cards);
    }
}
