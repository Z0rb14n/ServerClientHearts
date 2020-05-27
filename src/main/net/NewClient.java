package net;

import processing.core.PApplet;
import processing.net.Client;
import ui.SCHClient;
import ui.console.Console;
import ui.javaver.MainFrame;
import util.Deck;

import java.io.*;
import java.nio.ByteBuffer;

import static net.Constants.ERR_TIMED_OUT;
import static net.Constants.PORT;

// Represents the game client
public final class NewClient extends Client {
    private String clientID;
    private int playerNum;
    private boolean isUsingProcessing;

    // EFFECTS: initializes client without using processing
    public NewClient(String ip, boolean noUsingProcessing) {
        super(new PApplet(), ip, PORT);
        isUsingProcessing = false;
        if (!active()) {
            stop();
            Console.getConsole().addMessage("Could not connect to ip: " + ip + ", port: " + PORT);
            throw new ConnectionException(ERR_TIMED_OUT);
        } else {
            getClientIDFromServer();
        }
    }

    // EFFECTS: initializes client with params of Processing's Client parameters
    public NewClient(String ip) {
        super(SCHClient.getClient(), ip, PORT);
        isUsingProcessing = true;
        if (!active()) {
            stop();
            Console.getConsole().addMessage("Could not connect to ip: " + ip + ", port: " + PORT);
            throw new ConnectionException(ERR_TIMED_OUT);
        } else {
            getClientIDFromServer();
        }
    }

    // MODIFIES: this
    // EFFECTS: gets the client ID from the server
    //          throws ConnectionException if kicked from the server?
    private void getClientIDFromServer() {
        ServerToClientMessage msg = readServerToClientMessage();
        if (msg.isKickMessage()) {
            throw new ConnectionException(msg.getKickMessage());
        } else {
            clientID = msg.getID();
            playerNum = msg.getPlayerNumber();
            if (isUsingProcessing) SCHClient.getClient().catchExtraMessages(msg);
            else MainFrame.getFrame().catchExtraMessages(msg);
        }
    }

    private boolean msgFinished = true;

    // EFFECTS: returns the message sent by the server
    //    NOTE: THIS WILL COMPLETELY FREEZE EXECUTION UNTIL THE MESSAGE IS FULLY SENT.
    public ServerToClientMessage readServerToClientMessage() {
        if (!msgFinished)
            throw new RuntimeException("AAAA UR INTERNET IS SLOW AAAAA - tried to read a new message when one was already being read");
        msgFinished = false;
        int bytesRead = 0;
        while (available() < 4) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        int arrLength = readInt();
        if (arrLength < 0) throw new RuntimeException("NEGATIVE SIZE OF MESSAGE");
        byte[] msgBuffer = new byte[arrLength];
        while (bytesRead < msgBuffer.length) {
            byte[] arr = readBytes(msgBuffer.length - bytesRead);
            System.arraycopy(arr, 0, msgBuffer, bytesRead, arr.length);
            bytesRead += arr.length;
        }
        msgFinished = true;
        ByteArrayInputStream bis = new ByteArrayInputStream(msgBuffer);
        try (ObjectInputStream in = new ObjectInputStream(bis)) {
            return (ServerToClientMessage) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("Are you sure this is an actual server?");
            throw new RuntimeException(e.getMessage());
        }
    }

    public void write(ClientToServerMessage msg) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(msg);
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            writeInt(yourBytes.length);
            write(yourBytes);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private int readInt() {
        byte[] bytes = readBytes(4);
        return ByteBuffer.wrap(bytes).getInt();
    }

    private void writeInt(int a) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(a).array();
        write(bytes);
    }

    // EFFECTS: gets current client ID
    public String getClientID() {
        return clientID;
    }

    // EFFECTS: returns the player number
    public int getPlayerNum() {
        return playerNum;
    }

    // MODIFIES: this
    // EFFECTS: sends chat message to server
    public void sendChatMessage(String msg) {
        write(ClientToServerMessage.createNewChatMessage(msg));
    }

    // MODIFIES: this
    // EFFECTS: sends a card played message to server
    public void sendCardsPlayedMessage(Deck cards) {
        if (cards.deckSize() != 3 && cards.deckSize() != 1) throw new IllegalArgumentException();
        if (cards.deckSize() == 1) write(ClientToServerMessage.createNewCardPlayedMessage(cards.get(0)));
        if (cards.deckSize() == 3) write(ClientToServerMessage.createNewSubmitThreeCardMessage(cards));
    }

    @Override
    // MODIFIES: this
    // EFFECTS: disconnects client from the server and stops the client.
    public void stop() {
        ServerToClientMessage lastMessage = readServerToClientMessage();
        if (lastMessage.isKickMessage()) {
            if (isUsingProcessing) SCHClient.getClient().updateErrorMessage(lastMessage.getKickMessage());
            else MainFrame.getFrame().updateErrorMessage(lastMessage.getKickMessage());
        }

        super.stop();
    }
}
