package client.ui;

import java.util.Arrays;

/**
 * Represents the main entry point to run the client
 */
class Main {

    /**
     * Main method to run the client
     *
     * @param args -noconsole = runs without console
     */
    public static void main(String[] args) {

        if (Arrays.asList(args).contains("-noconsole")) ClientFrame.useConsole = false;
        ClientFrame.getFrame();
    }
}
