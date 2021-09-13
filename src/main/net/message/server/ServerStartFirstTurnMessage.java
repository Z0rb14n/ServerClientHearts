package net.message.server;

import org.jetbrains.annotations.NotNull;
import util.card.Deck;

import java.util.Objects;

public class ServerStartFirstTurnMessage implements ServerToClientMessage {
    private static final String FIRST_TURN_HEADER = "FIRST TURN: ";
    @NotNull
    private final Deck threeNewCards = new Deck();
    private final int startingPlayer;

    private ServerStartFirstTurnMessage() {
        startingPlayer = -1;
    }

    public ServerStartFirstTurnMessage(@NotNull Deck deck, int startingPlayer) {
        ServerToClientMessage.verifyDeckLength(deck, 3);
        ServerToClientMessage.verifyPlayerNumber(startingPlayer);
        threeNewCards.add(deck);
        this.startingPlayer = startingPlayer;
    }

    public @NotNull Deck getThreeNewCards() {
        return threeNewCards;
    }

    public int getStartingPlayer() {
        return startingPlayer;
    }

    @Override
    public boolean isValid() {
        return threeNewCards.size() == 3 && startingPlayer >= 1 && startingPlayer <= 4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerStartFirstTurnMessage)) return false;
        ServerStartFirstTurnMessage that = (ServerStartFirstTurnMessage) o;
        return startingPlayer == that.startingPlayer && Objects.equals(threeNewCards, that.threeNewCards);
    }

    @Override
    public String toString() {
        return FIRST_TURN_HEADER + "threeNewCards=" + threeNewCards +
                ", startingPlayer=" + startingPlayer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(threeNewCards, startingPlayer);
    }
}
