package net.message.server;

import util.card.Deck;

import java.util.Objects;

/**
 * Start next turn message, indicating the "winner" and the penalty cards s/he got
 */
public class ServerNextTurnMessage implements ServerToClientMessage {
    private static final long serialVersionUID = 1L;
    private static final String NEXT_TURN_HEADER = "NEXT TURN:";
    private final Deck newPenaltyCards = new Deck();
    private final int playerWhoStartsNext;

    public ServerNextTurnMessage(Deck newPenaltyCards, int playerWhoStartsNext) {
        this.newPenaltyCards.add(newPenaltyCards);
        this.playerWhoStartsNext = playerWhoStartsNext;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    public Deck getNewPenaltyCards() {
        return newPenaltyCards;
    }

    public int getPlayerWhoStartsNext() {
        return playerWhoStartsNext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerNextTurnMessage)) return false;
        ServerNextTurnMessage that = (ServerNextTurnMessage) o;
        return playerWhoStartsNext == that.playerWhoStartsNext && Objects.equals(newPenaltyCards, that.newPenaltyCards);
    }

    @Override
    public String toString() {
        return NEXT_TURN_HEADER +
                "newPenaltyCards=" + newPenaltyCards +
                ", playerWhoStartsNext=" + playerWhoStartsNext;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newPenaltyCards, playerWhoStartsNext);
    }
}
