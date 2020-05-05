package net;

import exception.InvalidClientNumberException;
import exception.InvalidDeckException;
import util.Card;
import util.Deck;
import util.Suit;

import java.io.Serializable;

// Represents the message sent from server to client
public final class ServerToClientMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // EFFECTS: throws IllegalArgumentException if player number is out of range
    private static void verifyPlayerNumber(int playerNum) {
        if (playerNum < 1 || playerNum > 4) throw new InvalidClientNumberException();
    }

    // EFFECTS: throws InvalidDeckException if deck length does not match
    private static void verifyDeckLength(Deck d, int length) {
        if (d.deckSize() != length) throw new InvalidDeckException();
    }

    // EFFECTS: blank constructor (produces invalid message, technically, so it can't be called)
    private ServerToClientMessage() {
    }

    // in order of which it'll likely be used

    // EFFECTS: determines whether this message is valid
    public boolean isValidMessage() {
        return !isKickMessage && !isIDMessage() && !isChatMessage() && !isPlayerConnectionMessage() && !isGameStartingMessage() && !isStartingFirstTurnMessage() && !isStartingNewTurnMessage() && !isNextCardMessage() && !isGameEndingMessage() && !isResetMessage();
    }

    // You have been kicked from connecting
    private boolean isKickMessage = false;
    private String kickMessage = "";

    // EFFECTS: creates a blank server to client message (invalid one)
    private static ServerToClientMessage createBlankMessage() {
        return new ServerToClientMessage();
    }

    public static ServerToClientMessage createKickMessage(String msg) {
        final ServerToClientMessage scm = createBlankMessage();
        scm.isKickMessage = true;
        scm.kickMessage = msg;
        return scm;
    }

    public boolean isKickMessage() {
        return isKickMessage;
    }

    // ID string + player number
    private String ID = "";
    private int playerNumber = 0;
    private boolean[] existingPlayers = new boolean[4];

    public static ServerToClientMessage createIDMessage(String UUID, int playerNum, boolean[] existingPlayers) {
        final ServerToClientMessage scm = createBlankMessage();
        verifyPlayerNumber(playerNum);
        if (UUID == null || UUID.length() < 1 || existingPlayers.length != 4) throw new IllegalArgumentException();
        if (!existingPlayers[0] && !existingPlayers[1] && !existingPlayers[2] && !existingPlayers[3])
            throw new IllegalArgumentException();
        scm.ID = UUID;
        scm.playerNumber = playerNum;
        scm.existingPlayers = existingPlayers;
        return scm;
    }

    public boolean isIDMessage() {
        return ID.length() > 0 && playerNumber != 0;
    }

    // Chat message
    private String chatMessage = "";
    private int playerChatSender = 0;

    public static ServerToClientMessage createChatMessage(String msg, int sender) {
        verifyPlayerNumber(sender);
        if (msg == null) throw new IllegalArgumentException();
        final ServerToClientMessage scm = createBlankMessage();
        scm.chatMessage = msg;
        scm.playerChatSender = sender;
        return scm;
    }

    public boolean isChatMessage() {
        return playerChatSender != 0;
    }

    // New player connection
    private boolean playerConnect = false;
    private int newPlayerNumber = 0;

    public static ServerToClientMessage createConnectionMessage(int playerNum) {
        verifyPlayerNumber(playerNum);
        final ServerToClientMessage scm = createBlankMessage();
        scm.playerConnect = true;
        scm.newPlayerNumber = playerNum;
        return scm;
    }

    public boolean isPlayerConnectionMessage() {
        return playerConnect && newPlayerNumber != 0;
    }

    // Player disconnected
    private boolean playerDisconnect = false;
    private int disconnectedPlayerNumber = 0;

    public static ServerToClientMessage createDisconnectMessage(int playerNum) {
        verifyPlayerNumber(playerNum);
        final ServerToClientMessage scm = createBlankMessage();
        scm.playerDisconnect = true;
        scm.disconnectedPlayerNumber = playerNum;
        return scm;
    }

    // Starting the game -> pass 3 cards and here's ur hand
    private boolean gameStarting = false;
    private Deck clientHand = new Deck();

    public static ServerToClientMessage createStartGameMessage(Deck deck) {
        verifyDeckLength(deck, 13);
        final ServerToClientMessage scm = createBlankMessage();
        scm.gameStarting = true;
        scm.clientHand = deck.copy();
        return scm;
    }

    public boolean isGameStartingMessage() {
        return gameStarting;
    }

    // Start first turn (get 3 cards, and who starts first)
    private Deck threeNewCards = new Deck();
    private boolean startingFirstTurnMessage = false;
    private boolean[] whichClientStarts = new boolean[4]; // determines who starts as a boolean [p1,p2,p3,p4]

    public static ServerToClientMessage createStartFirstTurnMessage(Deck threeCards, int startingPlayer) {
        verifyDeckLength(threeCards, 3);
        verifyPlayerNumber(startingPlayer);
        final ServerToClientMessage scm = createBlankMessage();
        scm.startingFirstTurnMessage = true;
        scm.threeNewCards = threeCards.copy();
        scm.whichClientStarts[startingPlayer - 1] = true;
        return scm;
    }

    public boolean isStartingFirstTurnMessage() {
        return startingFirstTurnMessage;
    }

    // New card played, who played it, the suit to play, and next player number
    private Card previouslyPlayed;
    private int playerNumJustPlayed = 0;
    private Suit expectedSuit;
    private int nextPlayerNumber = 0;

    public static ServerToClientMessage createRequestNextCardMessage(Card prevPlayed, int justPlayed, int nextToPlay, Suit requiredSuit) {
        verifyPlayerNumber(justPlayed);
        verifyPlayerNumber(nextToPlay);
        if (prevPlayed == null || requiredSuit == null) throw new IllegalArgumentException();
        final ServerToClientMessage scm = createBlankMessage();
        scm.previouslyPlayed = prevPlayed;
        scm.playerNumJustPlayed = justPlayed;
        scm.expectedSuit = requiredSuit;
        scm.nextPlayerNumber = nextToPlay;
        return scm;
    }

    public boolean isNextCardMessage() {
        return nextPlayerNumber != 0 && playerNumJustPlayed != 0 && previouslyPlayed != null && expectedSuit != null;
    }

    // Starting new turn (who got rekt by penalties, what penalties)
    private boolean startingNewTurn = false;
    private Deck newPenaltyCards = new Deck();
    private int playerWhoStartsNext = 0;

    public static ServerToClientMessage createStartNextTurnMessage(int winner, Deck newPenaltyCards) {
        verifyPlayerNumber(winner);
        if (newPenaltyCards.deckSize() > 4) throw new IllegalArgumentException();
        final ServerToClientMessage scm = createBlankMessage();
        scm.startingNewTurn = true;
        scm.newPenaltyCards = newPenaltyCards.copy();
        scm.playerWhoStartsNext = winner;
        return scm;
    }

    public boolean isStartingNewTurnMessage() {
        return startingNewTurn && playerWhoStartsNext != 0;
    }

    // End of game (who won, what penalty points they had, what cards they had)
    private boolean gameEnding = false;
    private boolean[] winners = new boolean[4];
    private Deck[] penaltyHands = new Deck[4];

    public static ServerToClientMessage createGameEndMessage(boolean[] winners, Deck[] penalties) {
        if (winners.length != 4 || penalties.length != 4) throw new IllegalArgumentException();
        final ServerToClientMessage scm = createBlankMessage();
        scm.gameEnding = true;
        System.arraycopy(winners, 0, scm.winners, 0, 4);
        System.arraycopy(penalties, 0, scm.penaltyHands, 0, 4);
        return scm;
    }

    public boolean isGameEndingMessage() {
        return gameEnding;
    }

    // Reset
    private boolean isReset = false;

    public static ServerToClientMessage createResetMessage() {
        final ServerToClientMessage scm = createBlankMessage();
        scm.isReset = true;
        return scm;
    }

    public boolean isResetMessage() {
        return isReset;
    }
}
