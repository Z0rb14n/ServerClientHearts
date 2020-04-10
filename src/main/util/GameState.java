package util;

import ui.ServerClientHearts;

import static util.PassOrder.ASCENDING_NUM;

// Represents the state of the server (i.e. has the game started?)
public class GameState {
    private static final PassOrder PASSING_ORDER = ASCENDING_NUM;
    private boolean isGameStarted;
    private boolean allCardsPassed;
    private int numTurns;
    private Suit currentSuitToPlay;
    private boolean threeOfClubsNeeded;
    private boolean gameEnded;

    private Deck[] hands;      // Hands of each player
    private Deck[] passingHands; // PassingHands of each player
    private Deck[] penalties;  // Penalty cards for each player
    private Deck center;       // center pile
    private int startingPlayer; // who played the first card, (1-4)

    // EFFECTS: initializes variables with default settings
    public GameState() {
        reset();
    }

    // EFFECTS: gets whether we're currently in the "currently passing 3 cards" stage
    public boolean isPassingCards() {
        return isGameStarted && !allCardsPassed;
    }

    // MODIFIES: this
    // EFFECTS: resets the game to start
    public void reset() {
        isGameStarted = false;
        allCardsPassed = false;
        numTurns = 0;
        center = new Deck();
        penalties = new Deck[4];
        passingHands = new Deck[4];
        hands = new Deck[4];
        currentSuitToPlay = null;
        startingPlayer = -1;
        threeOfClubsNeeded = false;
        gameEnded = false;
        initDecks();
    }

    // MODIFIES: this
    // EFFECTS: initializes decks in the arrays
    private void initDecks() {
        for (int i = 0; i < penalties.length; i++) {
            penalties[i] = new Deck();
        }
        for (int i = 0; i < hands.length; i++) {
            hands[i] = new Deck();
        }
        for (int i = 0; i < passingHands.length; i++) {
            passingHands[i] = new Deck();
        }
    }

    // MODIFIES: this
    // EFFECTS: starts the game
    public void startGame() {
        isGameStarted = true;
        Deck temp = new Deck();
        temp.generate52();
        temp.randomlyDistribute(hands[0], hands[1], hands[2], hands[3]);
    }

    //<editor-fold desc="Getters for individual hands">
    // EFFECTS: gets player one's hand
    public Deck getPlayerOneHand() {
        return hands[0];
    }

    // EFFECTS: gets player two's hand
    public Deck getPlayerTwoHand() {
        return hands[1];
    }

    // EFFECTS: gets player three's hand
    public Deck getPlayerThreeHand() {
        return hands[2];
    }

    // EFFECTS: gets player four's hand
    public Deck getPlayerFourHand() {
        return hands[3];
    }
    //</editor-fold>

    // EFFECTS: determines whether player number c (1-4) can play card (i.e. in their deck)
    private boolean isInvalidPlay(Card c, int playerNum) {
        if (playerNum < 1 || playerNum > 4)
            throw new IllegalArgumentException("Player number must be within 1-4: " + playerNum);
        return !hands[playerNum - 1].contains(c);
    }

    // REQUIRES: current suit != null
    // EFFECTS: determines whether the card being played actually follows the current suit
    private boolean matchesCurrentSuit(Card c, int playerNum) {
        if (playerNum < 1 || playerNum > 4)
            throw new IllegalArgumentException("Player number must be within 1-4: " + playerNum);
        if (currentSuitToPlay == null || c.getSuit().equals(currentSuitToPlay)) return true;
        return !hands[playerNum - 1].containsSuit(currentSuitToPlay);
    }

    // EFFECTS: determines whether the player (1-4) has played a card already (i.e. is in the center)
    private boolean hasPlayedCard(int playerNum) {
        if (startingPlayer > playerNum) playerNum += 4;
        return playerNum < startingPlayer + center.deckSize() && playerNum >= startingPlayer;
    }

    // MODIFIES: this, server
    // EFFECTS: receives the card played and updates game state, kicks player if invalid
    public void playCard(int playerNum, ServerClientHearts server, Card a, Card... c) {
        if (isInvalidPlay(a, playerNum)) server.kick(playerNum);
        for (Card card1 : c) {
            if (isInvalidPlay(card1, playerNum)) server.kick(playerNum);
        }
        if (!allCardsPassed) {
            if (c.length != 2) server.kick(playerNum); // you must be passing exactly three cards
            else if (!passingHands[playerNum - 1].isEmpty()) server.kick(playerNum); // you can't pass cards twice
            else {
                Deck newDeck = new Deck();
                newDeck.addCard(a);
                for (Card card : c) newDeck.addCard(card);
                passingHands[playerNum - 1] = newDeck;
                checkPassCards();
            }
        } else {
            if (c.length != 0) server.kick(playerNum); // you should not be playing more than one card
            else if (!matchesCurrentSuit(a, playerNum))
                server.kick(playerNum); // can't play clubs if suit is hearts, etc.
            else if (hasPlayedCard(playerNum)) server.kick(playerNum); // you can't play twice
            else if (threeOfClubsNeeded && !a.is3C()) server.kick(playerNum); // you have to play 3C if starting
            else {
                center.addCard(a);
                if (threeOfClubsNeeded) threeOfClubsNeeded = false;
                if (currentSuitToPlay == null) {
                    currentSuitToPlay = a.getSuit(); // if starting new turn, set new suit
                    startingPlayer = playerNum;
                }
                if (center.deckSize() == 4) endTurn(server);
            }
        }
    }

    // REQUIRES: center.size == 4
    // MODIFIES: this
    // EFFECTS: ends current turn and starts a new one
    private void endTurn(ServerClientHearts caller) {
        assert (center.deckSize() == 4);
        numTurns++;
        startingPlayer = trickWinner(); // set new starting player
        Deck deck = center.copy();
        deck.removeNonPenaltyCards();
        penalties[startingPlayer - 1].addAll(deck); // distribute penalties
        currentSuitToPlay = null; // can play whatever suit
        checkGameEnd(caller);
        if (!gameEnded) caller.startNewTurn(startingPlayer);
    }

    // REQUIRES: center.size == 4
    // EFFECTS: returns the player number that "won" the trick (1-4)
    private int trickWinner() {
        assert (center.deckSize() == 4);
        int index = center.highestIndexOfSuit(currentSuitToPlay);
        int playerWinner = index + startingPlayer;
        if (playerWinner > 4) playerWinner -= 4;
        return playerWinner;
    }

    // MODIFIES: this
    // EFFECTS: checks if all cards are passed. If so, actually pass cards and move on
    private void checkPassCards() {
        for (Deck d : passingHands) {
            if (d.isEmpty()) return;
        }
        if (PASSING_ORDER.equals(ASCENDING_NUM)) {
            hands[0].addAll(passingHands[3]);
            hands[1].addAll(passingHands[0]);
            hands[2].addAll(passingHands[1]);
            hands[3].addAll(passingHands[2]);
        } else {
            hands[0].addAll(passingHands[1]);
            hands[1].addAll(passingHands[2]);
            hands[2].addAll(passingHands[3]);
            hands[3].addAll(passingHands[0]);
        }
        allCardsPassed = true;
        startFirstTurn();
    }

    // MODIFIES: this
    // EFFECTS: checks whether the game has ended (i.e. turn number == 13)
    private void checkGameEnd(ServerClientHearts caller) {
        if (numTurns != 14) return;
        gameEnded = true;
        caller.endGame();
    }

    // MODIFIES: this
    // EFFECTS: starts the first turn - they MUST play three of clubs
    private void startFirstTurn() {
        threeOfClubsNeeded = true;
        numTurns = 1;
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
