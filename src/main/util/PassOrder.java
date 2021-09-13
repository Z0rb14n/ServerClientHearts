package util;

import org.jetbrains.annotations.Contract;

/**
 * Represents the passing order of the game
 */
public enum PassOrder {
    ASCENDING_NUM,
    DESCENDING_NUM,
    ODD_EVEN,
    LOW_HIGH;

    /**
     * Gets target player to pass to given player that's passing
     *
     * @param playerThatsPassing player who is passing the card
     * @return player number of player to pass to
     * @throws IllegalArgumentException if playerThatsPassing is out of range [1-4]
     */
    @Contract(pure = true)
    public int toPass(int playerThatsPassing) {
        if (playerThatsPassing < 1 || playerThatsPassing > 4) throw new IllegalArgumentException();
        if (this == ASCENDING_NUM) {
            if (playerThatsPassing == 4) return 1;
            else return playerThatsPassing + 1;
        } else if (this == DESCENDING_NUM) {
            if (playerThatsPassing == 1) return 4;
            else return playerThatsPassing - 1;
        } else if (this == ODD_EVEN) {
            if (playerThatsPassing == 4) return 2;
            else if (playerThatsPassing == 2) return 4;
            else if (playerThatsPassing == 3) return 1;
            else return 3;
        } else {
            if (playerThatsPassing == 4) return 3;
            else if (playerThatsPassing == 3) return 4;
            else if (playerThatsPassing == 2) return 1;
            else return 2;
        }
    }
}
