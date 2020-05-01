package net;

public class MessageConstants {
    //<editor-fold desc="MESSAGE HEADERS">
    public final static String ERROR = "ERR: ";
    public final static String NEW_PLAYER_HEADER = "NEW PLAYER:";
    public final static String DISCONNECT_PLAYER_HEADER = "DISCONNECT:";
    public final static String CURRENT_PLAYERS_HEADER = "CURRENT PLAYERS:";
    public final static String REQUEST_CARD_HEADER = "PLAY:";
    public final static String PREVIOUS_CARD_HEADER = "PLAYED:";
    public final static String PLAY_MSG_HEADER = "CARDS:";
    public final static String ROUND_WINNER_HEADER = "WINNER:";
    public final static String GAME_WINNER_HEADER = "GAME WINNER:";
    //</editor-fold>
    //<editor-fold desc="MESSAGE FORMATS">
    public final static String ERROR_FORMAT = "ERR: .+";
    public final static String CARD_DELIMITER = ",";
    public final static String REQUEST_CARD_MSG = REQUEST_CARD_HEADER + ".+";
    public final static String PREVIOUS_CARD_MSG = PREVIOUS_CARD_HEADER + "\\d,.+";
    public final static String ROUND_WINNER = ROUND_WINNER_HEADER + "\\d.*";
    public final static String GAME_WINNER = GAME_WINNER_HEADER + "\\d" + ",POINTS:\\d+";
    public final static String ERR_TOO_MANY_PLAYERS = ERROR + "TOO MANY PLAYERS";
    public final static String ERR_INVALID_MSG = ERROR + "INVALID MSG";
    public final static String NEW_PLAYER_MSG = NEW_PLAYER_HEADER + "\\d";
    public final static String DISCONNECT_PLAYER_MSG = DISCONNECT_PLAYER_HEADER + "\\d";
    public final static String KICK_DEFAULT_MSG = ERROR + "KICKED";
    public final static String RESET = "RESET";
    public final static String CURRENT_PLAYERS_MSG = CURRENT_PLAYERS_HEADER + "\\d*";
    public final static String START_GAME_MSG = "START GAME";
    public final static String CHAT_MSG_HEADER = "CHAT:";
    public final static String CHAT_MSG = CHAT_MSG_HEADER + ".+";
    public final static int CHAT_MSG_INDEX = CHAT_MSG_HEADER.length();
    public final static String OUTGOING_CHAT_MSG = "CHAT\\d:.+";
    public final static String PLAY_MSG = PLAY_MSG_HEADER + ".+";
    public final static int PLAY_MSG_INDEX = PLAY_MSG_HEADER.length();
    public final static String PLAYER_ID_HEADER = "P\\dID:.+";
    public final static String CENTER_HAND = "CENTER:";
    public final static String STARTING_HAND = "STARTING_HAND:";
    public final static String NEW_HAND = "NEW_HAND:";
    public final static String START_ROUND = "START_ROUND";
    public final static String END_ROUND = "END ROUND";
    public final static String END_GAME = "END GAME";
    public final static String START_3C = "START_3C";
    //</editor-fold>
}
