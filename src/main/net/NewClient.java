package net;

import processing.net.Client;
import ui.SCHClient;

import java.io.*;
import java.nio.ByteBuffer;

import static net.Constants.ERR_TIMED_OUT;

// Represents the game client
public final class NewClient extends Client {
    private static final int PORT = NewServer.PORT;
    private String clientID;
    private int playerNum;

    // EFFECTS: initializes client with params of Processing's Client parameters
    public NewClient(String ip) {
        super(SCHClient.getClient(), ip, PORT);
        if (!active()) {
            stop();
            System.out.println("Could not connect to ip: " + ip + ", port: " + PORT);
            throw new ConnectionException(ERR_TIMED_OUT);
        }
    }

    // MODIFIES: this
    // EFFECTS: initializes client ID
    public void initialize() {
        getClientIDFromServer();
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
            SCHClient.getClient().catchExtraMessages(msg);
        }

        System.out.println("Client ID is " + clientID + ", player num is " + playerNum);
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
    // EFFECTS; sends chat message to server
    public void sendChatMessage(String msg) {
        write(ClientToServerMessage.createNewChatMessage(msg));
    }

    @Override
    // MODIFIES: this
    // EFFECTS: disconnects client from the server and stops the client.
    public void stop() {
        ServerToClientMessage lastMessage = readServerToClientMessage();
        if (lastMessage.isKickMessage()) {
            SCHClient.getClient().updateErrorMessage(lastMessage.getKickMessage());
        }

        super.stop();
    }
}
