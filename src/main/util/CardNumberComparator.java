package util;

import java.util.Comparator;

public class CardNumberComparator implements Comparator<Card> {

    @Override
    public int compare(Card a, Card b) {
        return Integer.compare(a.getValue(), b.getValue());
    }
}
