package net.message.client;

import org.jetbrains.annotations.NotNull;
import util.card.Card;

import java.util.Objects;

public class ClientCardMessage implements ClientToServerMessage {
    private static final long serialVersionUID = 1L;
    private static final String NEW_CARD_HEADER = "NEW CARD:";
    private Card card;

    private ClientCardMessage() {
    }

    public ClientCardMessage(@NotNull Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    @Override
    public boolean isValid() {
        return card != null;
    }

    @Override
    public String toString() {
        return NEW_CARD_HEADER + card;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientCardMessage)) return false;
        ClientCardMessage that = (ClientCardMessage) o;
        return Objects.equals(card, that.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(card);
    }
}
