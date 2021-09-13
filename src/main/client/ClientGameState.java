package client;

import net.message.server.ServerStartFirstTurnMessage;
import util.PassOrder;
import util.PlayOrder;
import util.card.Card;
import util.card.Deck;
import util.card.Suit;

import java.io.Serializable;

public class ClientGameState implements Serializable {
    private static final long serialVersionUID = 69420L;

    private String clientID;
    int playerNumber = -1;
    private PlayOrder currentPlayOrder = null;
    private PassOrder currentPassOrder = null;
    private final Deck center = new Deck();
    private Suit requiredSuitToPlay = null;
    private final Deck receivedPassingHand = new Deck();
    Deck playerDeck = new Deck();
    private final Deck[] penalties = new Deck[4];
    private final int[] numCardsRemaining = new int[4];
    private final int[] numCardsInPassingHand = new int[4];
    private final boolean[] winners = new boolean[4];
    final boolean[] playersThatPassed = new boolean[4];
    private boolean gameStarted = false;
    private boolean gameEnded = false;
    private boolean startedPassingCards = false;
    private boolean allCardsPassed = false;
    private int nextToPlay = -1;
    private int firstPlayed = -1;
    private int numRoundsPlayed = 0;

    public ClientGameState() {
        reset();
    }

    public void reset() {
        currentPlayOrder = null;
        gameEnded = false;
        requiredSuitToPlay = null;
        currentPassOrder = null;
        center.clear();
        receivedPassingHand.clear();
        playerDeck.clear();
        for (int i = 0; i < 4; i++) {
            penalties[i] = new Deck();
            numCardsRemaining[i] = 13;
            numCardsInPassingHand[i] = 0;
            winners[i] = false;
            playersThatPassed[i] = false;
        }
        gameStarted = false;
        startedPassingCards = false;
        allCardsPassed = false;
        nextToPlay = -1;
        firstPlayed = -1;
        numRoundsPlayed = 0;
    }

    void startGame(Deck startingDeck) {
        playerDeck = startingDeck.copy();
        gameStarted = true;
        startedPassingCards = true;
        ClientLogger.logMessage("Player hand: " + startingDeck);
    }

    void finishPassingCards(ServerStartFirstTurnMessage ignored) {
        allCardsPassed = true;
    }

    void onNextTurn(Deck newPenalties, int player) {
        penalties[player - 1].add(newPenalties);
        nextToPlay = player;
        requiredSuitToPlay = null;
    }

    void onCardPlay(Card prevPlayed, int justPlayed, int nextToPlay, Suit requiredSuit) {
        this.nextToPlay = nextToPlay;
        if (firstPlayed == -1) firstPlayed = justPlayed;
        requiredSuitToPlay = requiredSuit;
        center.add(prevPlayed);
    }

    void setPlayerNumber(int number) {
        this.playerNumber = number;
    }

    void onGetID(String ID, int number) {
        this.playerNumber = number;
        this.clientID = ID;
    }

    void onGameEnd(boolean[] winners, Deck[] penalties) {
        gameEnded = true;
        System.arraycopy(winners, 0, this.winners, 0, 4);
        System.arraycopy(penalties, 0, this.penalties, 0, 4);
    }

    public boolean hasGameEnded() {
        return gameEnded;
    }

    public boolean[] getWinners() {
        assert (gameEnded);
        return winners;
    }

    public boolean isValidCardPass(Card c1, Card c2, Card c3) {
        return startedPassingCards && !allCardsPassed && playerDeck.contains(c1) && playerDeck.contains(c2) && playerDeck.contains(c3);
    }

    public boolean isValidCardPlay(Card c) {
        return canPlay() && playerDeck.contains(c);
    }

    public boolean canPlay() {
        return playerNumber == nextToPlay;
    }

    public boolean shouldPlayThreeOfClubs() {
        return center.size() == 0 && numRoundsPlayed == 0;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public PlayOrder getCurrentPlayOrder() {
        return currentPlayOrder;
    }

    public PassOrder getCurrentPassOrder() {
        return currentPassOrder;
    }

    public Deck getCenter() {
        return center.copy();
    }

    public Deck getReceivedPassingHand() {
        return receivedPassingHand.copy();
    }

    public Deck getPlayerDeck() {
        return playerDeck.copy();
    }

    public Deck[] getPenalties() {
        Deck[] decks = new Deck[4];
        for (int i = 0; i < 4; i++) {
            decks[i] = penalties[i].copy();
        }
        return decks;
    }

    public int[] getNumCardsRemaining() {
        int[] array = new int[4];
        System.arraycopy(numCardsRemaining, 0, array, 0, 4);
        return array;
    }

    public int[] getNumCardsInPassingHand() {
        int[] array = new int[4];
        System.arraycopy(numCardsInPassingHand, 0, array, 0, 4);
        return array;
    }

    public boolean[] getPlayersThatPassed() {
        return playersThatPassed;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isStartedPassingCards() {
        return startedPassingCards;
    }

    public boolean isAllCardsPassed() {
        return allCardsPassed;
    }

    public int getNextToPlay() {
        return nextToPlay;
    }

    public int getFirstPlayed() {
        return firstPlayed;
    }

    public int getNumRoundsPlayed() {
        return numRoundsPlayed;
    }
}
