package util;

public final class Util {
    public static boolean faceGreaterThan(Face a, Face b) {
        if (a.equals(Face.Ace)) return !b.equals(Face.Ace);
        if (a.equals(Face.King)) return (!b.equals(Face.Ace) && !b.equals(Face.King));
        if (a.equals(Face.Queen)) return (!b.equals(Face.Ace) && !b.equals(Face.King)&& !b.equals(Face.Queen));
        return false;
    }
    public static boolean faceGreaterThan(Card a, Card b) {
        if (a.getFace() == null) return false;
        if (b.getFace() == null) return true;
        return faceGreaterThan(a.getFace(), b.getFace());
    }
    public static boolean faceLessThan(Face a, Face b) {
        return !faceGreaterThan(a,b) && !a.equals(b);
    }
    public static boolean faceLessThan(Card a, Card b) {
        if (a.getFace() == null) return b.getFace() != null;
        if (b.getFace() == null) return false;
        return faceLessThan(a.getFace(), b.getFace());
    }
}
