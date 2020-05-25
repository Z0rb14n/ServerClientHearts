package util;

import ui.SCHServer;

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

    public Deck[] getHandsInOrder() {
        return hands;
    }

    public Deck[] getPassingHands() {
        return passingHands;
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

    // EFFECTS: gets the center hand
    public Deck getCenter() {
        return center;
    }

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

    // EFFECTS: determines whether the player is not the next player to play
    private boolean isPlayingOutOfOrder(int playernum) {
        return playernum != nextToPlay();
    }

    // EFFECTS: determines the player number of player next to play
    private int nextToPlay() {
        int next = center.deckSize() + startingPlayer;
        return next > 4 ? next - 4 : next;
    }

    // MODIFIES: this, server
    // EFFECTS: receives the card played and updates game state, kicks player if invalid
    public void playCard(int playerNum, SCHServer server, Card a, Card... c) {
        if (isInvalidPlay(a, playerNum)) {
            server.requestKickInvalidMessage(playerNum);
            return;
        }
        if (!isGameStarted) {
            server.requestKickInvalidMessage(playerNum);
            return;
        }
        for (Card card1 : c) {
            if (isInvalidPlay(card1, playerNum)) {
                server.requestKickInvalidMessage(playerNum);
                return;
            }
        }
        if (!allCardsPassed) {
            if (c.length != 2) {
                server.requestKickInvalidMessage(playerNum); // you must be passing exactly three cards
                return;
            } else if (!passingHands[playerNum - 1].isEmpty()) {
                server.requestKickInvalidMessage(playerNum); // you can't pass cards twice
                return;
            } else {
                Deck newDeck = new Deck();
                newDeck.addCard(a);
                for (Card card : c) {
                    newDeck.addCard(card);
                    hands[playerNum - 1].removeCard(card);
                }
                passingHands[playerNum - 1] = newDeck;
                checkPassCards(server);
            }
        } else {
            if (c.length != 0)
                server.requestKickInvalidMessage(playerNum); // you should not be playing more than one card
            else if (!matchesCurrentSuit(a, playerNum))
                server.requestKickInvalidMessage(playerNum); // can't play clubs if suit is hearts, etc.
            else if (hasPlayedCard(playerNum)) server.requestKickInvalidMessage(playerNum); // you can't play twice
            else if (threeOfClubsNeeded && !a.is3C())
                server.requestKickInvalidMessage(playerNum); // you have to play 3C if starting
            else if (isPlayingOutOfOrder(playerNum))
                server.requestKickInvalidMessage(playerNum); // can't play out of order
            else {
                center.addCard(a);
                if (threeOfClubsNeeded) threeOfClubsNeeded = false;
                if (currentSuitToPlay == null) {
                    currentSuitToPlay = a.getSuit(); // if starting new turn, set new suit
                    startingPlayer = playerNum;
                }
                if (center.deckSize() == 4) endTurn(server);
                else {
                    server.requestNextCard(playerNum, nextToPlay(), a, currentSuitToPlay);
                }
            }
        }
    }

    // REQUIRES: center.size == 4
    // MODIFIES: this
    // EFFECTS: ends current turn and starts a new one
    private void endTurn(SCHServer caller) {
        assert (center.deckSize() == 4);
        numTurns++;
        startingPlayer = trickWinner(); // set new starting player
        Deck deck = center.copy();
        deck.removeNonPenaltyCards();
        penalties[startingPlayer - 1].addAll(deck); // distribute penalties
        currentSuitToPlay = null; // can play whatever suit
        checkGameEnd(caller);
        if (!gameEnded) caller.startNewTurn(startingPlayer, deck);
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
    private void checkPassCards(SCHServer caller) {
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
        startFirstTurn(caller);
    }

    // REQUIRES: game has JUST started
    // EFFECTS: returns the player that should start first (i.e. has 3C)
    private int gameStarter() {
        if (hands[0].containsThreeOfClubs()) return 1;
        if (hands[1].containsThreeOfClubs()) return 2;
        if (hands[2].containsThreeOfClubs()) return 3;
        if (hands[3].containsThreeOfClubs()) return 4;
        throw new RuntimeException("Could not find Three of Clubs");
    }

    // MODIFIES: this
    // EFFECTS: checks whether the game has ended (i.e. turn number == 13)
    private void checkGameEnd(SCHServer caller) {
        if (numTurns != 14) return;
        gameEnded = true;
        caller.endGame(gameWinner(), gameWinnerPoints(), penalties);
    }

    // EFFECTS: returns current winner
    private boolean[] gameWinner() {
        boolean[] result = new boolean[4];
        int minimum = gameWinnerPoints();
        if (minimum == hands[0].penaltyPoints()) result[0] = true;
        if (minimum == hands[1].penaltyPoints()) result[1] = true;
        if (minimum == hands[2].penaltyPoints()) result[2] = true;
        if (minimum == hands[3].penaltyPoints()) result[3] = true;
        return result;
    }

    // EFFECTS: returns point count of current winner
    private int gameWinnerPoints() {
        return Math.min(Math.min(hands[0].penaltyPoints(), hands[1].penaltyPoints()), Math.min(hands[2].penaltyPoints(), hands[3].penaltyPoints()));
    }

    // MODIFIES: this
    // EFFECTS: starts the first turn - they MUST play three of clubs
    private void startFirstTurn(SCHServer caller) {
        threeOfClubsNeeded = true;
        startingPlayer = gameStarter();
        caller.startFirstTurn(startingPlayer);
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
