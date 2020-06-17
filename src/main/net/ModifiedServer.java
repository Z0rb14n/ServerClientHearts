package net;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

// Represents Processing's Server but modified to not use PApplet
public class ModifiedServer implements Runnable {
    protected EventReceiver eventReceiver;

    private volatile Thread thread;
    private ServerSocket server;

    private final Object clientsLock = new Object[0];
    /**
     * ArrayList of client objects
     */
    private ArrayList<ModifiedClient> clients = new ArrayList<>(10);

    /**
     * @param parent typically use "this"
     * @param port   port used to transfer data
     */
    public ModifiedServer(EventReceiver parent, int port) {
        this.eventReceiver = parent;
        try {
            server = new ServerSocket(port);
            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            thread = null;
            throw new RuntimeException(e);
        }
    }


    /**
     * ( begin auto-generated from Server_disconnect.xml )
     * <p>
     * Disconnect a particular client.
     * <p>
     * ( end auto-generated )
     *
     * @param client the client to disconnect
     * @brief Disconnect a particular client.
     * @webref server:server
     */
    public void disconnect(ModifiedClient client) {
        client.stop();
        synchronized (clientsLock) {
            int index = clientIndex(client);
            if (index != -1) {
                removeIndex(index);
            }
        }
    }


    protected void removeIndex(int index) {
        synchronized (clientsLock) {
            clients.remove(index);
        }
    }


    protected void disconnectAll() {
        synchronized (clientsLock) {
            while (!clients.isEmpty()) {
                try {
                    clients.get(0).stop();
                } catch (Exception ignored) {
                }
                clients.remove(0);
            }
        }
    }


    protected void addClient(ModifiedClient client) {
        synchronized (clientsLock) {
            clients.add(client);
        }
    }


    protected int clientIndex(ModifiedClient client) {
        synchronized (clientsLock) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i) == client) return i;
            }
            return -1;
        }
    }


    /**
     * ( begin auto-generated from Server_active.xml )
     * <p>
     * Returns true if this server is still active and hasn't run
     * into any trouble.
     * <p>
     * ( end auto-generated )
     *
     * @webref server:server
     * @brief Return true if this server is still active.
     */
    public boolean active() {
        return thread != null;
    }


    static public String ip() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }


    // the last index used for available. can't just cycle through
    // the clients in order from 0 each time, because if client 0 won't
    // shut up, then the rest of the clients will never be heard from.
    int lastAvailable = -1;

    /**
     * ( begin auto-generated from Server_available.xml )
     * <p>
     * Returns the next client in line with a new message.
     * <p>
     * ( end auto-generated )
     *
     * @brief Returns the next client in line with a new message.
     * @webref server
     * @usage application
     */
    public ModifiedClient available() {
        synchronized (clientsLock) {
            int index = lastAvailable + 1;
            if (index >= clients.size()) index = 0;

            for (int i = 0; i < clients.size(); i++) {
                int which = (index + i) % clients.size();
                ModifiedClient client = clients.get(which);
                //Check for valid client
                if (!client.active()) {
                    removeIndex(which);  //Remove dead client
                    i--;                 //Don't skip the next client
                    //If the client has data make sure lastAvailable
                    //doesn't end up skipping the next client
                    which--;
                    //fall through to allow data from dead clients
                    //to be retreived.
                }
                if (client.available() > 0) {
                    lastAvailable = which;
                    return client;
                }
            }
        }
        return null;
    }


    /**
     * ( begin auto-generated from Server_stop.xml )
     * <p>
     * Disconnects all clients and stops the server.
     * <p>
     * ( end auto-generated )
     * <h3>Advanced</h3>
     * Use this to shut down the server if you finish using it while your applet
     * is still running. Otherwise, it will be automatically be shut down by the
     * host PApplet using dispose(), which is identical.
     *
     * @brief Disconnects all clients and stops the server.
     * @webref server
     * @usage application
     */
    public void stop() {
        dispose();
    }


    /**
     * Disconnect all clients and stop the server: internal use only.
     */
    public void dispose() {
        thread = null;

        if (clients != null) {
            disconnectAll();
            clients = null;
        }
        try {
            if (server != null) {
                server.close();
                server = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        while (Thread.currentThread() == thread) {
            try {
                Socket socket = server.accept();
                ModifiedClient client = new ModifiedClient(eventReceiver, socket);
                synchronized (clientsLock) {
                    addClient(client);
                    eventReceiver.clientConnectionEvent(this, client);
                }
            } catch (SocketException e) {
                //thrown when server.close() is called and server is waiting on accept
                System.err.println("Server SocketException: " + e.getMessage());
                thread = null;
            } catch (IOException e) {
                //errorMessage("run", e);
                e.printStackTrace();
                thread = null;
            }
        }
    }


    /**
     * ( begin auto-generated from Server_write.xml )
     * <p>
     * Writes a value to all the connected clients. It sends bytes out from the
     * Server object.
     * <p>
     * ( end auto-generated )
     *
     * @param data data to write
     * @webref server
     * @brief Writes data to all connected clients
     */
    public void write(int data) {  // will also cover char
        synchronized (clientsLock) {
            int index = 0;
            while (index < clients.size()) {
                if (clients.get(index).active()) {
                    clients.get(index).write(data);
                    index++;
                } else {
                    removeIndex(index);
                }
            }
        }
    }


    public void write(byte[] data) {
        synchronized (clientsLock) {
            int index = 0;
            while (index < clients.size()) {
                if (clients.get(index).active()) {
                    clients.get(index).write(data);
                    index++;
                } else {
                    removeIndex(index);
                }
            }
        }
    }


    public void write(String data) {
        synchronized (clientsLock) {
            int index = 0;
            while (index < clients.size()) {
                if (clients.get(index).active()) {
                    clients.get(index).write(data);
                    index++;
                } else {
                    removeIndex(index);
                }
            }
        }
    }
}
