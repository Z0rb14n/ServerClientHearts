package net;

import processing.core.PApplet;
import processing.net.Client;
import ui.ServerClientHearts;

public class NewClient extends Client {
    public static final String CLIENT_ID_MSG = "P\\dID:(.+)";
    public static final String TOO_MANY_PLAYERS = ServerClientHearts.ERR_TOO_MANY_PLAYERS;
    public static final String ERR_INVALID_MSG = ServerClientHearts.ERR_INVALID_MSG;
    public boolean actuallyInitialized = false;
    String clientID;
    private int playerNum;

    public NewClient(PApplet pa, String ip, int port) {
        super(pa, ip, port);
        if (active()) getClientID();
        else {
            stop();
            System.out.println("Could not connect to ip: " + ip + ", port: " + port);
            throw new ConnectionException();
        }
        actuallyInitialized = true;
    }

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

    public int getPlayerNum() {
        return playerNum;
    }

    @Override
    public void stop() {
        if (clientID != null) write(clientID + ":DISCONNECT");
        super.stop();
    }

}
