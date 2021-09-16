package client;

import net.message.server.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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
    final Deck playerDeck = new Deck();
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

    ///<editor-fold desc="ServerMessage Handlers">

    /**
     * Internally starts the game
     *
     * @param message Initial ServerStartGameMessage
     */
    @Contract(mutates = "this")
    void startGame(@NotNull ServerStartGameMessage message) {
        playerDeck.clear();
        playerDeck.add(message.getClientHand());
        gameStarted = true;
        startedPassingCards = true;
        ClientLogger.logMessage("[ClientGameStart::startGame]: Player hand: " + playerDeck);
    }

    /**
     * Signals that cards are finished passing and 3C must be played
     *
     * @param message Message received - given 3 cards
     */
    @Contract(mutates = "this")
    void finishPassingCards(ServerStartFirstTurnMessage message) {
        allCardsPassed = true;
        playerDeck.add(message.getThreeNewCards());
        nextToPlay = message.getStartingPlayer();
        requiredSuitToPlay = Suit.Club;
    }

    /**
     * Starts on the next turn
     *
     * @param message Message received
     */
    @Contract(mutates = "this")
    void onNextTurn(ServerNextTurnMessage message) {
        penalties[message.getPlayerWhoStartsNext() - 1].add(message.getNewPenaltyCards());
        nextToPlay = message.getPlayerWhoStartsNext();
        requiredSuitToPlay = null;
        numRoundsPlayed++;
    }

    /**
     * Runs upon a player playing a card
     *
     * @param message Message received
     */
    @Contract(mutates = "this")
    void onCardPlay(ServerRequestNextCardMessage message) {
        nextToPlay = message.getNextPlayerNumber();
        if (firstPlayed == -1) firstPlayed = message.getPlayerNumJustPlayed();
        requiredSuitToPlay = message.getExpectedSuit();
        center.add(message.getPreviouslyPlayed());
    }

    /**
     * Runs upon the ID message received
     *
     * @param message Message received
     */
    @Contract(mutates = "this")
    void onIDMessage(@NotNull ServerIDMessage message) {
        clientID = message.getID();
        playerNumber = message.getPlayerNumber();
    }

    /**
     * Runs upon server game message received - resets the internal state
     *
     * @param message ServerGameResetMessage received
     */
    @Contract(mutates = "this")
    void onGameReset(@NotNull ServerGameResetMessage message) {
        if (message.isValid()) reset();
    }

    /**
     * Runs upon game end message received
     *
     * @param message Message received
     */
    @Contract(mutates = "this")
    void onGameEnd(@NotNull ServerGameEndMessage message) {
        gameEnded = true;
        System.arraycopy(message.getWinners(), 0, winners, 0, 4);
        for (int i = 0; i < 4; i++) penalties[i] = message.getPenaltyHands()[i].copy();
    }
    ///</editor-fold>

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
        if (shouldPlayThreeOfClubs() && !new Card(Suit.Club, 3).equals(c)) return false;
        if (!canPlay() || !playerDeck.contains(c)) return false;
        if (requiredSuitToPlay == null) return true;
        return !playerDeck.containsSuit(requiredSuitToPlay) || c.getSuit() == requiredSuitToPlay;
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
        return playerDeck;
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
