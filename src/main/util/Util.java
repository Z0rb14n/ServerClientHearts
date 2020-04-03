package util;

public final class Util {
    public static boolean faceGreaterThan(Face a, Face b) {
        if (a.equals(Face.Ace)) return !b.equals(Face.Ace);
        if (a.equals(Face.King)) return (!b.equals(Face.Ace) && !b.equals(Face.King));
        if (a.equals(Face.Queen)) return (!b.equals(Face.Ace) && !b.equals(Face.King)&& !b.equals(Face.Queen));
        return false;
    }
    public static boolean faceGreaterThan(Card a, Card b) {
        return faceGreaterThan(a.getFace(), b.getFace());
    }
    public static boolean faceLessThan(Face a, Face b) {
        return !faceGreaterThan(a,b) && !a.equals(b);
    }
    public static boolean faceLessThan(Card a, Card b) {
        return faceLessThan(a.getFace(), b.getFace());
    }
    public static int getValue(Face a) {
        if (a.equals(Face.Ace)) return 14;
        if (a.equals(Face.King)) return 13;
        if (a.equals(Face.Queen)) return 12;
        if (a.equals(Face.Jack)) return 11;
        throw new IllegalArgumentException();
    }
    public static int getValue(Card a) {
        if (!a.isFaceCard()) return a.getNumber();
        return getValue(a.getFace());
    }
}
