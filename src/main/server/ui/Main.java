package server.ui;

// TODO Disconnect inform not being present on kick

import server.GameServer;

/**
 * Entry point to run the server
 */
class Main {
    public static final boolean HEADLESS = true;

    /**
     * Main Method to instantiate a server
     *
     * @param args ignored command-line arguments
     */
    public static void main(String[] args) {
        if (HEADLESS) {
            GameServer server = new GameServer();
            //noinspection InfiniteLoopStatement
            while (true) {
                server.update();
                try {
                    //noinspection BusyWait
                    Thread.sleep(30);
                } catch (InterruptedException ignored) {
                }
            }
        } else {
            ServerFrame.getInstance();
        }
    }
}
