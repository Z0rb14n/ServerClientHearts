package net;

import processing.net.Client;
import ui.ServerClientHearts;
import ui.ServerClientHeartsClient;

// Represents the game client
public final class NewClient extends Client {
    public static final String CLIENT_ID_MSG = MessageConstants.CLIENT_ID_MESSAGE;
    public static final String TOO_MANY_PLAYERS = MessageConstants.ERR_TOO_MANY_PLAYERS;
    public static final String ERR_INVALID_MSG = MessageConstants.ERR_INVALID_MSG;
    public static final String ERR_KICKED = MessageConstants.KICK_DEFAULT_MSG;
    private static final String ERROR = MessageConstants.ERROR;
    private static final int PORT = ServerClientHearts.PORT;
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

    // EFFECTS: gets current client ID
    public String getClientID() {
        return clientID;
    }

    // EFFECTS: returns the player number
    public int getPlayerNum() {
        return playerNum;
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
