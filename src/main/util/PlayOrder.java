package util;

import org.jetbrains.annotations.Contract;

/**
 * Represents the order in which cards are played (not to be confused with PassOrder)
 */
public enum PlayOrder {
    ASCENDING_NUM(true), // i.e. player 3 then player 4 then 1
    DESCENDING_NUM(false); // i.e. player 4 then player 1 then 2
    private final boolean increasing;

    /**
     * Initializes the play order given whether the number should be increasing or decreasing
     *
     * @param inc boolean indicating increasing or decreasing player numbers
     */
    PlayOrder(boolean inc) {
        this.increasing = inc;
    }

    // EFFECTS: gets the next player to play given the player that just played
    //          throws IllegalArgumentException if player number is not in range

    /**
     * Gets teh next player to play given previous player that just played
     *
     * @param previousPlayer player number of previous player that played
     * @return player number of next player
     * @throws IllegalArgumentException if player number is out of range [1-4]
     */
    @Contract(pure = true)
    public int nextPlayer(int previousPlayer) {
        if (previousPlayer < 1 || previousPlayer > 4)
            throw new IllegalArgumentException("Previous player is out of range [1-4]: " + previousPlayer);
        if (increasing && previousPlayer == 4) return 1;
        else if (!increasing && previousPlayer == 1) return 4;
        if (increasing) return previousPlayer + 1;
        else return previousPlayer - 1;
    }

    /**
     * Gets the next player to play given the first player and the number of cards played
     *
     * @param firstPlayer    first played that played
     * @param numCardsPlayed number of cards played
     * @return played number to play next
     * @throws IllegalArgumentException if player number is not in range [1-4]
     * @throws IllegalArgumentException numCardsPlayed is not in range [0-3]
     */
    @Contract(pure = true)
    public int nextPlayer(int firstPlayer, int numCardsPlayed) {
        if (firstPlayer < 1 || firstPlayer > 4 || numCardsPlayed < 0 || numCardsPlayed > 3)
            throw new IllegalArgumentException();
        if (increasing) {
            firstPlayer += numCardsPlayed;
            if (firstPlayer > 4) firstPlayer -= 4;
        } else {
            firstPlayer -= numCardsPlayed;
            if (firstPlayer < 1) firstPlayer += 4;
        }
        return firstPlayer;
    }

    /**
     * Determines if player in question has played
     *
     * @param firstPlayer      first player to play
     * @param numCardsPlayed   number of cards played
     * @param playerInQuestion player in question
     * @return true if player should have played given number of cards played
     * @throws IllegalArgumentException if player number is not in range [1-4]
     * @throws IllegalArgumentException if card played number is out of range [0-3]
     */
    public boolean hasPlayerPlayed(int firstPlayer, int numCardsPlayed, int playerInQuestion) {
        if (firstPlayer < 1 || firstPlayer > 4 || numCardsPlayed < 0 || numCardsPlayed > 3 || playerInQuestion < 1 || playerInQuestion > 4)
            throw new IllegalArgumentException();
        int currentPlayer = firstPlayer;
        for (; numCardsPlayed > 0; numCardsPlayed--) {
            if (currentPlayer == playerInQuestion) return true;
            currentPlayer = nextPlayer(currentPlayer);
        }
        return false;
    }
}
