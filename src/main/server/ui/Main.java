package server.ui;

import net.ModifiedNewServer;
import server.GameServer;

/**
 * Entry point to run the server
 */
public class Main {
    public static final boolean HEADLESS = true;
    public static GameServer server;

    /**
     * Main Method to instantiate a server
     *
     * @param args ignored command-line arguments
     */
    public static void main(String[] args) {
        if (HEADLESS) {
            server = new GameServer();
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

    /**
     * Shorthand to return the active GameServer (a singleton is probably not a good idea)
     *
     * @return active GameServer
     */
    public static GameServer getGameServer() {
        if (HEADLESS) return server;
        return ServerFrame.getInstance().gameServer;
    }

    /**
     * Shorthand to return the active ModifiedNewServer (a singleton is probably not a good idea)
     *
     * @return active ModifiedNewServer
     */
    public static ModifiedNewServer getNetServer() {
        return getGameServer().getNetServer();
    }
}
