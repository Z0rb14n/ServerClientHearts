package net;

import ui.client.MainFrame;
import ui.console.Console;
import util.Deck;

import java.io.*;
import java.nio.ByteBuffer;

import static net.Constants.*;

public final class ModifiedNewClient extends ModifiedClient {
    private String clientID;
    private int playerNum;

    public ModifiedNewClient(EventReceiver parent, String ip) throws ConnectionException {
        super(parent, ip, Constants.PORT);
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
    //          throws ConnectionException if kicked from the server
    private void getClientIDFromServer() {
        // continuously wait until you get a message
        while (available() <= 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        ServerToClientMessage msg = readServerToClientMessage();
        assert (msg != null);
        if (msg.isKickMessage()) {
            throw new ConnectionException(ERR_KICKED + msg.getKickMessage());
        } else {
            clientID = msg.getID();
            playerNum = msg.getPlayerNumber();
            MainFrame.getFrame().catchExtraMessages(msg);
        }
    }

    private boolean msgFinished = true;

    // EFFECTS: returns the message sent by the server
    //    NOTE: THIS WILL COMPLETELY FREEZE EXECUTION UNTIL THE MESSAGE IS FULLY SENT.
    public ServerToClientMessage readServerToClientMessage() {
        if (!msgFinished)
            throw new RuntimeException("Slow Internet - tried to read a new message when one was already being read");
        msgFinished = false;
        int bytesRead = 0;
        if (available() == 0) {
            Console.getConsole().addMessage("Tried to read ServerToClientMessage when available() == 0. Returning null.");
            return null;
        }
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
            ServerToClientMessage scm = (ServerToClientMessage) in.readObject();
            if (scm.isKickMessage()) {
                Console.getConsole().addMessage("Received kick message from server: " + scm.getKickMessage());
            }
            return scm;
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("Are you sure this is an actual server?");
            throw new RuntimeException(e.getMessage());
        }
    }

    // MODIFIES: this
    // EFFECTS: writes out a ClientToServerMessage to the server
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

    // EFFECTS: reads the next four bytes from the server and interprets it as an int
    private int readInt() {
        byte[] bytes = readBytes(4);
        return ByteBuffer.wrap(bytes).getInt();
    }

    // EFFECTS: writes out the byte representation of an integer to the server
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
        if (cards.size() != 3 && cards.size() != 1) throw new IllegalArgumentException();
        if (cards.size() == 1) write(ClientToServerMessage.createNewCardPlayedMessage(cards.get(0)));
        if (cards.size() == 3) write(ClientToServerMessage.createNewSubmitThreeCardMessage(cards));
    }

    @Override
    // MODIFIES: this
    // EFFECTS: disconnects client from the server and stops the client.
    public void stop() {
        ServerToClientMessage lastMessage = readServerToClientMessage();
        if (lastMessage != null && lastMessage.isKickMessage()) {
            MainFrame.getFrame().updateErrorMessage(lastMessage.getKickMessage());
        }
        super.stop();
    }
}
