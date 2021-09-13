package net;

public final class Constants {
    public static final long DEFAULT_TIMEOUT = 5000;
    static final int MAX_LENGTH = 0x0FFFFFFF;

    public static final String ERR_KICKED = "ERR: KICK. MSG: ";
    public static final String ERR_TOO_MANY_PLAYERS = "ERR: TOO MANY PLAYERS";
    public static final String ERR_INVALID_MSG = "ERR: INVALID MESSAGE";
    public static final String ERR_TIMED_OUT = "Timed out.";

    public static boolean isTimeoutMessage(String str) {
        return ERR_TIMED_OUT.equals(str.trim());
    }

    public static boolean isKickMessage(String str) {
        if (str == null) return false;
        return str.startsWith(ERR_KICKED);
    }

    public final static char SPADE_UNICODE = '♠';
    // public final static char SPADE_UNICODE_OUTLINE = '♤';
    public final static char CLUB_UNICODE = '♣';
    // public final static char CLUB_UNICODE_OUTLINE = '♧';
    public final static char DIAMOND_UNICODE = '♦';
    // public final static char DIAMOND_UNICODE_OUTLINE = '♢';
    public final static char HEART_UNICODE = '♥';
    // public final static char HEART_UNICODE_OUTLINE = '♡';

    final static int PORT = 5204;
}
