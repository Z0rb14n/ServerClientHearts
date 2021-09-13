package net.message.server;

import util.Deck;

import java.util.Arrays;

public class ServerGameEndMessage implements ServerToClientMessage {
    private static final long serialVersionUID = 1L;
    private static final String GAME_END_HEADER = "GAME END:";
    private final boolean[] winners = new boolean[4];
    private final Deck[] penaltyHands = new Deck[4];

    private ServerGameEndMessage() {
    }

    public ServerGameEndMessage(Deck[] penaltyHands, boolean[] winners) {
        if (winners.length != 4 || penaltyHands.length != 4) throw new IllegalArgumentException();
        System.arraycopy(winners, 0, this.winners, 0, 4);
        System.arraycopy(penaltyHands, 0, this.penaltyHands, 0, 4);
    }

    @Override
    public boolean isValid() {
        return false;
    }

    public boolean[] getWinners() {
        boolean[] array = new boolean[4];
        System.arraycopy(winners, 0, array, 0, 4);
        return array;
    }

    public Deck[] getPenaltyHands() {
        Deck[] array = new Deck[4];
        System.arraycopy(penaltyHands, 0, array, 0, 4);
        return array;
    }

    @Override
    public String toString() {
        return GAME_END_HEADER +
                "winners=" + Arrays.toString(winners) +
                ", penaltyHands=" + Arrays.toString(penaltyHands) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerGameEndMessage)) return false;
        ServerGameEndMessage that = (ServerGameEndMessage) o;
        return Arrays.equals(winners, that.winners) && Arrays.equals(penaltyHands, that.penaltyHands);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(winners);
        result = 31 * result + Arrays.hashCode(penaltyHands);
        return result;
    }
}
