package net;

import processing.net.Client;
import ui.ServerClientHeartsClient;

import java.io.*;

// Represents the game client
public final class NewClient extends Client {
    public static final String CLIENT_ID_MSG = MessageConstants.CLIENT_ID_MESSAGE;
    public static final String TOO_MANY_PLAYERS = MessageConstants.ERR_TOO_MANY_PLAYERS;
    public static final String ERR_INVALID_MSG = MessageConstants.ERR_INVALID_MSG;
    public static final String ERR_KICKED = MessageConstants.KICK_DEFAULT_MSG;
    private static final String ERROR = MessageConstants.ERROR;
    private static final int PORT = NewServer.PORT;
    public boolean actuallyInitialized;
    private ServerClientHeartsClient caller;
    private String clientID;
    private int playerNum;

    // EFFECTS: initializes client with params of Processing's Client parameters
    public NewClient(ServerClientHeartsClient pa, String ip) {
        super(pa, ip, PORT);
        caller = pa;
        if (active()) getClientIDFromServer();
        else {
            stop();
            System.out.println("Could not connect to ip: " + ip + ", port: " + PORT);
            throw new ConnectionException();
        }
        actuallyInitialized = true;
    }

    // MODIFIES: this
    // EFFECTS: gets the client ID from the server
    //          throws ConnectionException if kicked from the server?
    private void getClientIDFromServer() {
        String idString = null;
        while (idString == null) {
            idString = readString();
        }
        if (idString.matches(TOO_MANY_PLAYERS)) {
            throw new ConnectionException(TOO_MANY_PLAYERS);
        }
        if (!idString.matches(CLIENT_ID_MSG)) {
            throw new ConnectionException(ERR_INVALID_MSG);
        }
        clientID = idString.substring(5);
        if (clientID.contains(MessageConstants.CURRENT_PLAYERS_HEADER)) {
            for (int i = clientID.length() - 1; i > 0; i--) {
                if (clientID.charAt(i) == MessageConstants.CURRENT_PLAYERS_HEADER.charAt(1)) {
                    int index = i - 1;
                    caller.catchAccidentalCurrentPlayersMessage(clientID.substring(index).trim());
                    clientID = clientID.substring(0, index);
                    break;
                }
            }
        }
        playerNum = Character.digit(idString.charAt(1), 10);
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
        int arrLength = read();
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
            throw new RuntimeException(e.getMessage());
        }
    }

    public void write(ClientToServerMessage msg) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(msg);
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            write(yourBytes);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
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
        write(MessageConstants.CHAT_MSG_HEADER + msg);
    }

    @Override
    // MODIFIES: this
    // EFFECTS: disconnects client from the server and stops the client.
    public void stop() {
        String lastMessage = readString();
        if (lastMessage != null && lastMessage.matches(MessageConstants.ERROR_FORMAT))
            caller.updateErrorMessage(lastMessage);
        super.stop();
    }
}
