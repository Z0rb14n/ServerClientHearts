package net;

import util.Card;
import util.Deck;
import util.Suit;

import java.io.Serializable;

// Represents the message sent from server to client
public class ServerToClientMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    // in order of which it'll likely be used

    // You have been kicked from connecting
    boolean isKickMessage = false;
    String kickMessage = "";

    // ID string + player number
    String ID = "";
    int playerNumber = 0;

    // Chat message
    String chatMessage = "";
    int playerChatSender = 0;

    // New player connection
    boolean playerConnect = false;
    int newPlayerNumber = 0;

    // Player disconnected
    boolean playerDisconnect = false;
    int disconnectedPlayerNumber = 0;

    // Starting the game -> pass 3 cards and here's ur hand
    boolean gameStarting = false;
    Deck clientHand = new Deck();

    // Start first turn (get 3 cards, and who starts first)
    Deck threeNewCards = new Deck();
    boolean startingFirstTurn = false;
    boolean[] whichClientStarts = new boolean[4]; // determines who starts as a boolean [p1,p2,p3,p4]

    // New card played, who played it, the suit to play, and next player number
    Card previouslyPlayed;
    int playerNumJustPlayed = 0;
    Suit expectedSuit;
    int nextPlayerNumber = 0;

    // Starting new turn (who got rekt by penalties, what penalties)
    boolean startingNewTurn = false;
    Deck newPenaltyCards = new Deck();
    int playerWhoGetsPenalties = 0;

    // End of game (who won, what penalty points they had, what cards they had)
    boolean gameEnding = false;
    boolean[] winners = new boolean[4];
    int[] penaltyPoints = new int[4];
    Deck[] penaltyHands = new Deck[4];

    // Reset
    boolean isReset = false;
}
