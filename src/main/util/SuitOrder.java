package util;

import java.util.Comparator;

import static util.Suit.*;

public class SuitOrder implements Comparator<Card> {
    boolean sortByValue;
    Suit top;
    Suit secondTop;
    Suit secondBottom;
    Suit bottom;
    public SuitOrder() {
        // Defaults: Hearts > Diamonds > Spades > Clubs
        sortByValue = false;
        top = Heart;
        secondTop = Diamond;
        secondBottom = Spade;
        bottom = Club;
    }
    public boolean isSortingByValue() {
        return sortByValue;
    }
    public void setSortByValue(boolean val) {
        sortByValue = val;
    }
    public void moveSuitToTop(Suit a) {
        int location = locateSuit(a);
        if (location == 1) return;
        Suit temp = top;
        top = a;
        // shifts everything down one
        if (location == 4) bottom = secondBottom;
        if (location == 3) secondBottom = secondTop;
        if (location == 2) secondTop = temp;
    }
    public void moveSuitToBottom(Suit a) {
        int location = locateSuit(a);
        if (location == 4) return;
        Suit temp = bottom;
        bottom = a;
        if (location == 1) top = secondTop;
        if (location == 2) secondTop = secondBottom;
        if (location == 3) secondBottom = temp;
    }
    public int locateSuit(Suit a) {
        if (top.equals(a)) return 1;
        if (secondTop.equals(a)) return 2;
        if (secondBottom.equals(a)) return 3;
        if (bottom.equals(a)) return 4;
        throw new IllegalArgumentException();
    }
    
    public int suitCompare(Card a, Card b) {
        return suitCompare(a.getSuit(),b.getSuit());
    }
    public int suitCompare(Suit a, Suit b) {
        if (a.equals(b)) return 0;
        if (a.equals(top)) return 1;
        if (a.equals(bottom)) return -1;
        if (a.equals(secondTop)) {
            if (b.equals(top)) return -1;
            else return 1;
        }
        if (a.equals(secondBottom)) {
            if (b.equals(bottom)) return 1;
            else return -1;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int compare(Card a, Card b) {
        CardNumberComparator lol = new CardNumberComparator();
        if (sortByValue) return lol.compare(a,b);
        if (suitCompare(a,b) != 0) return suitCompare(a,b);
        else return lol.compare(a,b);
    }
}
