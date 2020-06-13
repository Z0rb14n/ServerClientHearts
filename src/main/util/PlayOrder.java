package util;

// EFFECTS: represents the order of cards to play (i.e. which player plays next, not same as pass or)
public enum PlayOrder {
    ASCENDING_NUM(true), // i.e. player 3 then player 4 then 1
    DESCENDING_NUM(false); // i.e. player 4 then player 1 then 2
    private boolean increasing;

    PlayOrder(boolean inc) {
        this.increasing = inc;
    }

    // EFFECTS: gets the next player to play given the player that just played
    //          throws IllegalArgumentException if player number is not in range
    public int nextPlayer(int previousPlayer) {
        if (previousPlayer < 1 || previousPlayer > 4) throw new IllegalArgumentException();
        if (increasing && previousPlayer == 4) return 1;
        else if (!increasing && previousPlayer == 1) return 4;
        if (increasing) return previousPlayer + 1;
        else return previousPlayer - 1;
    }

    // EFFECTS: gets the next player to play given the first player and the number of cards played
    //          throws IllegalArgumentException if player number is not in range, or numCardsPlayed is not in range (0-3)
    public int nextPlayer(int firstPlayer, int numCardsPlayed) {
        if (firstPlayer < 1 || firstPlayer > 4 || numCardsPlayed < 0 || numCardsPlayed > 3)
            throw new IllegalArgumentException();
        if (increasing) {
            firstPlayer += numCardsPlayed;
            if (firstPlayer > 4) firstPlayer -= 4;
            return firstPlayer;
        } else {
            firstPlayer -= numCardsPlayed;
            if (firstPlayer < 1) firstPlayer += 4;
            return firstPlayer;
        }
    }

    // EFFECTS: determines if the player in question has played
    //          throws IllegalArgumentException if player numbers or card played number is out of range (1-4,0-3 respectively)
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
