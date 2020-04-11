package net;

import processing.net.Client;
import ui.ServerClientHearts;
import ui.ServerClientHeartsClient;

// Represents the game client
public class NewClient extends Client {
    public static final String CLIENT_ID_MSG = "P\\dID:(.+)";
    public static final String TOO_MANY_PLAYERS = ServerClientHearts.ERR_TOO_MANY_PLAYERS;
    public static final String ERR_INVALID_MSG = ServerClientHearts.ERR_INVALID_MSG;
    public static final String ERR_KICKED = "Kicked";
    private static final String ERROR = ServerClientHearts.ERROR;
    private static final int PORT = ServerClientHearts.PORT;
    public boolean actuallyInitialized = false;
    private ServerClientHeartsClient caller;
    private String lastMessage;
    String clientID;
    private int playerNum;

    // EFFECTS: initializes client with params of Processing's Client parameters
    public NewClient(ServerClientHeartsClient pa, String ip) {
        super(pa, ip, PORT);
        caller = pa;
        if (active()) getClientID();
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
    private void getClientID() {
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
        playerNum = idString.charAt(1) - '0';
        System.out.println("Client ID is " + clientID + ", player num is " + playerNum);
    }

    // EFFECTS: returns the player number
    public int getPlayerNum() {
        return playerNum;
    }

    @Override
    // MODIFIES: this
    // EFFECTS: disconnects client from the server and stops the client.
    public void stop() {
        lastMessage = readString();
        if (lastMessage.matches(ServerClientHearts.ERROR_FORMAT)) caller.updateErrorMessage(lastMessage);
        super.stop();
    }
}
