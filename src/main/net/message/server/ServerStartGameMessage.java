package net.message.server;

import util.PassOrder;
import util.PlayOrder;
import util.card.Deck;

import java.util.Objects;

/**
 * Represents a message sent by the server upon the game starting (so the players must send 3 cards after)
 */
public class ServerStartGameMessage implements ServerToClientMessage {
    private static final long serialVersionUID = 1L;
    private static final String GAME_START_HEADER = "GAME_START: ";
    private final Deck clientHand = new Deck();
    private PlayOrder playOrder;
    private PassOrder passOrder;

    private ServerStartGameMessage() {
    }

    public ServerStartGameMessage(Deck deck, PlayOrder playOrder, PassOrder passOrder) {
        ServerToClientMessage.verifyDeckLength(deck, 13);
        clientHand.add(deck);
        this.passOrder = passOrder;
        this.playOrder = playOrder;
    }

    public Deck getClientHand() {
        return clientHand;
    }

    public PlayOrder getPlayOrder() {
        return playOrder;
    }

    public PassOrder getPassOrder() {
        return passOrder;
    }

    @Override
    public boolean isValid() {
        return clientHand.size() == 13;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerStartGameMessage)) return false;
        ServerStartGameMessage that = (ServerStartGameMessage) o;
        return Objects.equals(clientHand, that.clientHand);
    }

    @Override
    public String toString() {
        return GAME_START_HEADER + clientHand;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientHand);
    }
}
