package net.message.server;

import org.jetbrains.annotations.NotNull;
import util.card.Card;
import util.card.Suit;

import java.util.Objects;

/**
 * Server message to inform a client that a player has played and another card must be played by some player
 */
public class ServerRequestNextCardMessage implements ServerToClientMessage {
    private static final long serialVersionUID = 1L;
    private static final String NEXT_CARD_HEADER = "NEXT CARD:";
    private final Card previouslyPlayed;
    private final int playerNumJustPlayed;
    private final Suit expectedSuit;
    private final int nextPlayerNumber;

    public ServerRequestNextCardMessage() {
        previouslyPlayed = null;
        playerNumJustPlayed = -1;
        expectedSuit = null;
        nextPlayerNumber = -1;
    }

    public ServerRequestNextCardMessage(@NotNull Card previouslyPlayed, int playerNumJustPlayed, @NotNull Suit expectedSuit, int nextPlayerNumber) {
        this.previouslyPlayed = previouslyPlayed.copy();
        this.playerNumJustPlayed = playerNumJustPlayed;
        this.expectedSuit = expectedSuit;
        this.nextPlayerNumber = nextPlayerNumber;
    }

    @Override
    public boolean isValid() {
        return previouslyPlayed != null &&
                previouslyPlayed.isValid() &&
                ServerToClientMessage.playerNumberInRange(playerNumJustPlayed) &&
                ServerToClientMessage.playerNumberInRange(nextPlayerNumber) &&
                expectedSuit != null;
    }

    public int getNextPlayerNumber() {
        return nextPlayerNumber;
    }

    public Suit getExpectedSuit() {
        return expectedSuit;
    }

    public int getPlayerNumJustPlayed() {
        return playerNumJustPlayed;
    }

    public Card getPreviouslyPlayed() {
        return previouslyPlayed;
    }

    @Override
    public String toString() {
        return NEXT_CARD_HEADER + "previouslyPlayed=" + previouslyPlayed +
                ", playerNumJustPlayed=" + playerNumJustPlayed +
                ", expectedSuit=" + expectedSuit +
                ", nextPlayerNumber=" + nextPlayerNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerRequestNextCardMessage)) return false;
        ServerRequestNextCardMessage that = (ServerRequestNextCardMessage) o;
        return playerNumJustPlayed == that.playerNumJustPlayed && nextPlayerNumber == that.nextPlayerNumber && Objects.equals(previouslyPlayed, that.previouslyPlayed) && expectedSuit == that.expectedSuit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(previouslyPlayed, playerNumJustPlayed, expectedSuit, nextPlayerNumber);
    }
}
