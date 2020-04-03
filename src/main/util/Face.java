package util;

public enum Face {
    Jack {
        @Override
        public String toString() {
            return "J";
        }
        public int getValue() {
            return 11;
        }
    },
    Queen {
        @Override
        public String toString() {
            return "Q";
        }
        public int getValue() {
            return 12;
        }
    },
    King {
        @Override
        public String toString() {
            return "K";
        }
        public int getValue() {
            return 13;
        }
    },
    Ace {
        @Override
        public String toString() {
            return "A";
        }
        public int getValue() {
            return 14;
        }
    }
}
