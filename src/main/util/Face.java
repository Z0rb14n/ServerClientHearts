package util;

public enum Face {

    Jack {
        @Override
        public String toString() {
            return "J";
        }

        @Override
        public int getValue() {
            return 11;
        }
    },
    Queen {
        @Override
        public String toString() {
            return "Q";
        }

        @Override
        public int getValue() {
            return 12;
        }
    },
    King {
        @Override
        public String toString() {
            return "K";
        }

        @Override
        public int getValue() {
            return 13;
        }
    },
    Ace {
        @Override
        public String toString() {
            return "A";
        }

        @Override
        public int getValue() {
            return 14;
        }
    };

    public int getValue() {
        return -1;
    }
}
