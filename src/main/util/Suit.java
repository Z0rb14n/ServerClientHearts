package main.util;

public enum Suit {
    Club {
        @Override
        public String toString() {
            return "C";
        }
    },
    Heart {
        @Override
        public String toString() {
            return "H";
        }
    },
    Diamond {
        @Override
        public String toString() {
            return "D";
        }
    },
    Spade {
        @Override
        public String toString() {
            return "S";
        }
    }
}
