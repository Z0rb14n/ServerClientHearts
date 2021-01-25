package net;

import util.Card;
import util.Deck;
import util.Suit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

// Represents the message sent from server to client
public final class ServerToClientMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String INVALID_MSG = "INVALID MESSAGE";
    private static final String KICK_HEADER = "KICK:";
    private static final String ID_HEADER = "ID:";
    private static final String CHAT_HEADER = "CHAT:"; // "CHAT:\\d,.*"
    private static final String PLAYER_CONNECT_HEADER = "CONNECT:";
    private static final String PLAYER_DISCONNECT_HEADER = "DISCONNECT:";
    private static final String GAME_START_HEADER = "GAME_START:";
    private static final String FIRST_TURN_HEADER = "FIRST TURN:";
    private static final String NEXT_CARD_HEADER = "NEXT CARD:";
    private static final String NEXT_TURN_HEADER = "NEXT TURN:";
    private static final String GAME_END_HEADER = "GAME END:";
    private static final String RESET_MSG = "RESET";

    // EFFECTS: returns the byte array representation of this message to be sent
    byte[] toByteArr() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
            oos.flush();
            byte[] bytes = bos.toByteArray();
            if (bytes.length > Constants.MAX_LENGTH) throw new RuntimeException("AAAAAAAAAAAAAAAAAAA");
            ByteBuffer bb = ByteBuffer.allocate(4 + bytes.length);
            bb.putInt(bytes.length);
            bb.put(bytes);
            return bb.array();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // EFFECTS: throws IllegalArgumentException if player number is out of range
    private static void verifyPlayerNumber(int playerNum) {
        if (playerNum < 1 || playerNum > 4) throw new IllegalArgumentException();
    }

    // EFFECTS: throws IllegalArgumentException if deck length does not match
    private static void verifyDeckLength(Deck d, int length) {
        if (d.size() != length) throw new IllegalArgumentException();
    }

    // These functions are listed in the order in which they will be used.

    // EFFECTS: determines whether this message is valid
    public boolean isValidMessage() {
        return isKickMessage || isIDMessage() || isChatMessage() || isPlayerConnectionMessage() || isGameStartingMessage() || isStartingFirstTurnMessage() || isStartingNewTurnMessage() || isNextCardMessage() || isGameEndingMessage() || isResetMessage();
    }

    // You have been kicked from connecting
    private boolean isKickMessage = false;
    private String kickMessage = null;

    // EFFECTS: creates a blank server to client message (invalid one)
    private static ServerToClientMessage createBlankMessage() {
        return new ServerToClientMessage();
    }

    // EFFECTS: creates a kick message with a given message
    static ServerToClientMessage createKickMessage(String msg) {
        if (msg == null) throw new IllegalArgumentException();
        final ServerToClientMessage scm = createBlankMessage();
        scm.isKickMessage = true;
        scm.kickMessage = msg;
        return scm;
    }

    // EFFECTS: returns whether this message is a kick message
    public boolean isKickMessage() {
        return isKickMessage && kickMessage != null;
    }

    // EFFECTS: gets the kick message
    String getKickMessage() {
        return kickMessage;
    }

    // ID string + player number
    private String ID = null;
    private int playerNumber = 0;
    private boolean[] existingPlayers = new boolean[4];

    // EFFECTS: creates an ID message
    public static ServerToClientMessage createIDMessage(String UUID, int playerNum, boolean[] existingPlayers) {
        final ServerToClientMessage scm = createBlankMessage();
        verifyPlayerNumber(playerNum);
        if (UUID == null || UUID.length() < 1 || existingPlayers.length != 4) throw new IllegalArgumentException();
        scm.ID = UUID;
        scm.playerNumber = playerNum;
        scm.existingPlayers = existingPlayers;
        return scm;
    }

    // EFFECTS: returns the player number of the ID message
    public int getPlayerNumber() {
        return playerNumber;
    }

    // EFFECTS: returns whether this message is an ID message
    public boolean isIDMessage() {
        return ID != null && ID.length() > 0 && playerNumber != 0;
    }

    // EFFECTS: returns the ID if the message is an ID message
    String getID() {
        return ID;
    }

    // EFFECTS: returns existing player if the message is an ID message
    public boolean[] getExistingPlayers() {
        return existingPlayers;
    }

    // Chat message
    private String chatMessage = "";
    private int playerChatSender = 0;

    // EFFECTS: creates a chat message given a sender and the message
    public static ServerToClientMessage createChatMessage(String msg, int sender) {
        verifyPlayerNumber(sender);
        if (msg == null) throw new IllegalArgumentException();
        final ServerToClientMessage scm = createBlankMessage();
        scm.chatMessage = msg;
        scm.playerChatSender = sender;
        return scm;
    }

    // EFFECTS: returns whether or not this message is a chat message
    public boolean isChatMessage() {
        return playerChatSender != 0;
    }

    // EFFECTS: gets the sent chat message
    public String getChatMessage() {
        return chatMessage;
    }

    // EFFECTS: gets the player number of the sender of the chat message
    public int getChatMessageSender() {
        return playerChatSender;
    }

    // New player connection
    private boolean playerConnect = false;
    private int newPlayerNumber = 0;

    // EFFECTS: creates a connection message (i.e. player X connected)
    public static ServerToClientMessage createConnectionMessage(int playerNum) {
        verifyPlayerNumber(playerNum);
        final ServerToClientMessage scm = createBlankMessage();
        scm.playerConnect = true;
        scm.newPlayerNumber = playerNum;
        return scm;
    }

    // EFFECTS: returns whether or not this message is a player connection message
    public boolean isPlayerConnectionMessage() {
        return playerConnect && newPlayerNumber != 0;
    }

    // EFFECTS: gets the player number of the new connected player
    public int getNewConnectedPlayer() {
        return newPlayerNumber;
    }

    // Player disconnected
    private boolean playerDisconnect = false;
    private int disconnectedPlayerNumber = 0;

    // EFFECTS: creates a new player disconnect message
    public static ServerToClientMessage createDisconnectMessage(int playerNum) {
        verifyPlayerNumber(playerNum);
        final ServerToClientMessage scm = createBlankMessage();
        scm.playerDisconnect = true;
        scm.disconnectedPlayerNumber = playerNum;
        return scm;
    }

    // EFFECTS: returns whether this message is a player disconnection message
    public boolean isPlayerDisconnectMessage() {
        return playerDisconnect && disconnectedPlayerNumber != 0;
    }

    // EFFECTS: returns the player number of the person who disconnected
    public int getDisconnectedPlayerNumber() {
        return disconnectedPlayerNumber;
    }

    // Starting the game -> pass 3 cards and here's ur hand
    private boolean gameStarting = false;
    private Deck clientHand = new Deck();

    // EFFECTS: creates a new game start message (and the deck of cards)
    public static ServerToClientMessage createStartGameMessage(Deck deck) {
        verifyDeckLength(deck, 13);
        final ServerToClientMessage scm = createBlankMessage();
        scm.gameStarting = true;
        scm.clientHand = deck.copy();
        return scm;
    }

    // EFFECTS: returns whether this message as a game starting message
    public boolean isGameStartingMessage() {
        return gameStarting;
    }

    // EFFECTS: gets the starting hand
    public Deck getStartingHand() {
        return clientHand;
    }

    // Start first turn (get 3 cards, and who starts first)
    private Deck threeNewCards = new Deck();
    private boolean startingFirstTurnMessage = false;
    private final boolean[] whichClientStarts = new boolean[4]; // determines who starts as a boolean [p1,p2,p3,p4]

    // EFFECTS: creates a start first turn message, with the three cards the player receives, and the starting player
    static ServerToClientMessage createStartFirstTurnMessage(Deck threeCards, int startingPlayer) {
        verifyDeckLength(threeCards, 3);
        verifyPlayerNumber(startingPlayer);
        final ServerToClientMessage scm = createBlankMessage();
        scm.startingFirstTurnMessage = true;
        scm.threeNewCards = threeCards.copy();
        scm.whichClientStarts[startingPlayer - 1] = true;
        return scm;
    }

    // EFFECTS: determines if this message is a start first turn message
    public boolean isStartingFirstTurnMessage() {
        return startingFirstTurnMessage;
    }

    // New card played, who played it, the suit to play, and next player number
    private Card previouslyPlayed;
    private int playerNumJustPlayed = 0;
    private Suit expectedSuit;
    private int nextPlayerNumber = 0;

    // EFFECTS: creates a request new card message, given previous card/player and next suit/player
    static ServerToClientMessage createRequestNextCardMessage(Card prevPlayed, int justPlayed, int nextToPlay, Suit requiredSuit) {
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

    // EFFECTS: returns whether or not this message is a next card message
    public boolean isNextCardMessage() {
        return nextPlayerNumber != 0 && playerNumJustPlayed != 0 && previouslyPlayed != null && expectedSuit != null;
    }

    // Starting new turn (who got rekt by penalties, what penalties)
    private boolean startingNewTurn = false;
    private Deck newPenaltyCards = new Deck();
    private int playerWhoStartsNext = 0;

    // EFFECTS: creates a start next turn message, indicating the "winner" and the penalty cards s/he got
    static ServerToClientMessage createStartNextTurnMessage(int winner, Deck newPenaltyCards) {
        verifyPlayerNumber(winner);
        if (newPenaltyCards.size() > 4) throw new IllegalArgumentException();
        final ServerToClientMessage scm = createBlankMessage();
        scm.startingNewTurn = true;
        scm.newPenaltyCards = newPenaltyCards.copy();
        scm.playerWhoStartsNext = winner;
        return scm;
    }

    // EFFECTS: determines if this message is one to indicate a new turn starting
    public boolean isStartingNewTurnMessage() {
        return startingNewTurn && playerWhoStartsNext != 0;
    }

    // End of game (who won, what penalty points they had, what cards they had)
    private boolean gameEnding = false;
    private final boolean[] winners = new boolean[4];
    private final Deck[] penaltyHands = new Deck[4];

    // EFFECTS: creates a game end message
    static ServerToClientMessage createGameEndMessage(boolean[] winners, Deck[] penalties) {
        if (winners.length != 4 || penalties.length != 4) throw new IllegalArgumentException();
        final ServerToClientMessage scm = createBlankMessage();
        scm.gameEnding = true;
        System.arraycopy(winners, 0, scm.winners, 0, 4);
        System.arraycopy(penalties, 0, scm.penaltyHands, 0, 4);
        return scm;
    }

    // EFFECTS: determines if this message is a game end message
    public boolean isGameEndingMessage() {
        return gameEnding;
    }

    // Reset
    private boolean isReset = false;

    // EFFECTS: creates a reset message
    static ServerToClientMessage createResetMessage() {
        final ServerToClientMessage scm = createBlankMessage();
        scm.isReset = true;
        return scm;
    }

    // EFFECTS: returns whether or not this message is a reset message
    public boolean isResetMessage() {
        return isReset;
    }

    @Override
    // EFFECTS: returns string representation of this message
    public String toString() {
        if (!isValidMessage()) return INVALID_MSG;
        if (isKickMessage()) {
            return KICK_HEADER + getKickMessage();
        } else if (isIDMessage()) {
            return ID_HEADER + getID() + "," + existingPlayers[0] + existingPlayers[1] + existingPlayers[2] + existingPlayers[3];
        } else if (isChatMessage()) {
            return CHAT_HEADER + getChatMessageSender() + "," + getChatMessage();
        } else if (isPlayerConnectionMessage()) {
            return PLAYER_CONNECT_HEADER + getNewConnectedPlayer();
        } else if (isPlayerDisconnectMessage()) {
            return PLAYER_DISCONNECT_HEADER + getDisconnectedPlayerNumber();
        } else if (isGameStartingMessage()) {
            return GAME_START_HEADER + clientHand.toString();
        } else if (isStartingFirstTurnMessage()) {
            return FIRST_TURN_HEADER + threeNewCards.toString() + "," + whichClientStarts[0] + whichClientStarts[1] + whichClientStarts[2] + whichClientStarts[3];
        } else if (isNextCardMessage()) {
            return NEXT_CARD_HEADER + previouslyPlayed.toString() + "," + playerNumJustPlayed + "," + expectedSuit.toString() + "," + nextPlayerNumber;
        } else if (isStartingNewTurnMessage()) {
            return NEXT_TURN_HEADER + newPenaltyCards.toString() + "," + playerWhoStartsNext;
        } else if (isGameEndingMessage()) {
            return GAME_END_HEADER + winners[0] + winners[1] + winners[2] + winners[3] + penaltyHands[0].toString() + penaltyHands[1].toString() + penaltyHands[2].toString() + penaltyHands[3].toString();
        } else if (isResetMessage()) {
            return RESET_MSG;
        }
        return INVALID_MSG;
    }
}
