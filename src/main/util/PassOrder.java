package util;

// Represents the passing order
public enum PassOrder {
    ASCENDING_NUM,
    DESCENDING_NUM,
    ODD_EVEN,
    LOW_HIGH;

    // EFFECTS: gets the target player to pass to
    //          throws IllegalArgumentException if player number is out of range
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
