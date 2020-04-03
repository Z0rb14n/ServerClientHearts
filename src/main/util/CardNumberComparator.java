package util;

import java.util.Comparator;

public class CardNumberComparator implements Comparator<Card> {

    @Override
    public int compare(Card a, Card b) {
        if (a.isFaceCard() && !b.isFaceCard()) {
            return 1;
        }
        if (!a.isFaceCard() && b.isFaceCard()) {
            return -1;
        }
        if (a.isFaceCard() && b.isFaceCard()) {
            if (Util.faceGreaterThan(a, b)) {
                return 1;
            } else if (a.getFace().equals(b.getFace())) {
                return 0;
            } else {
                return -1;
            }
        }
        if (a.getNumber() > b.getNumber()) {
            return 1;
        } else if (a.getNumber() == b.getNumber()) {
            return 0;
        } else {
            return -1;
        }
    }
}
