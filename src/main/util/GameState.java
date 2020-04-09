package util;

// Represents the state of the server (i.e. has the game started?)
public class GameState {
    private boolean isGameStarted;
    private boolean allCardsPassed;
    private int numTurns;
    private Suit currentSuitToPlay;

    // EFFECTS: initializes variables with default settings
    public GameState() {
        isGameStarted = false;
        allCardsPassed = false;
        numTurns = 0;
    }

    // EFFECTS: gets whether we're currently in the "currently passing 3 cards" stage
    public boolean isPassingCards() {
        return isGameStarted && !allCardsPassed;
    }

    // MODIFIES: this
    // EFFECTS: starts the game
    public void startGame() {
        isGameStarted = true;
    }

    // MODIFIES: this
    // EFFECTS: stops the game
    public void stopGame() {
        isGameStarted = false;
    }

    // MODIFIES: this
    // EFFECTS: finishes passing the cards
    public void finishPassingCards() {
        allCardsPassed = true;
    }

    // MODIFIES: this
    // EFFECTS: increments the turn counter
    public void newTurn() {
        numTurns++;
    }

    // EFFECTS: returns whether the game has started
    public boolean isGameStarted() {
        return isGameStarted;
    }

    // EFFECTS: returns whether all the cards have passed (e.g. left/right/across)
    public boolean isAllCardsPassed() {
        return allCardsPassed;
    }

    // EFFECTS: gets the current turn number
    public int currentTurnNumber() {
        return numTurns;
    }

    // EFFECTS: returns the current active suit
    public Suit getCurrentSuitToPlay() {
        return currentSuitToPlay;
    }
}
