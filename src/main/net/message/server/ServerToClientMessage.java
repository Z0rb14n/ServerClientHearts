package net.message.server;

import net.message.NetworkMessage;
import util.card.Deck;

public interface ServerToClientMessage extends NetworkMessage {
    /**
     * Throws an exception if playerNumberInRange is false
     *
     * @param playerNum Player number in question
     * @throws IllegalArgumentException if player number is outside of range
     */
    static void verifyPlayerNumber(int playerNum) {
        if (!playerNumberInRange(playerNum)) throw new IllegalArgumentException();
    }

    /**
     * Determines whether player number is within range [1-4]
     *
     * @param playerNum Player number in question
     * @return whether the player number is within range [1-4]
     */
    static boolean playerNumberInRange(int playerNum) {
        return playerNum >= 1 && playerNum <= 4;
    }

    /**
     * Throws IllegalArgumentException if deck length does not match
     *
     * @param d      Deck
     * @param length Required length of deck
     * @throws IllegalArgumentException if deck length does not match
     */
    static void verifyDeckLength(Deck d, int length) {
        if (d.size() != length) throw new IllegalArgumentException();
    }
}
