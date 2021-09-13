package ui.server;

// TODO move ServerFrame logic to its own class (i.e. non-UI code not in UI)

/**
 * Entry point to run the server
 */
class Main {
    /**
     * Main Method to instantiate a server
     *
     * @param args ignored command-line arguments
     */
    public static void main(String[] args) {
        ServerFrame.getInstance();
    }
}
