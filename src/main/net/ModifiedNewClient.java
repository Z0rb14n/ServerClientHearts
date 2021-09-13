package net;

import client.console.Console;
import client.ui.ClientFrame;
import net.message.NetworkMessage;
import net.message.client.ClientToServerMessage;
import net.message.server.ServerIDMessage;
import net.message.server.ServerKickMessage;
import net.message.server.ServerToClientMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static net.Constants.*;

public final class ModifiedNewClient extends ModifiedClient implements EventReceiver {
    private final ObjectEventReceiver objectEventReceiver;
    private final Queue<ServerToClientMessage> lastMessages = new ArrayDeque<>(10);
    private String clientID;
    private int playerNum;

    private final ReentrantLock lastMessagesMutex = new ReentrantLock();
    private final Condition lastMessagesCondition = lastMessagesMutex.newCondition();

    public ModifiedNewClient(ObjectEventReceiver parent, String ip) throws ConnectionException {
        super(null, ip, Constants.PORT);
        setEventReceiver(this);
        objectEventReceiver = parent;
        if (!active()) {
            stop();
            if (ClientFrame.useConsole)
                Console.getConsole().addMessage("Could not connect to ip: " + ip + ", port: " + PORT);
            throw new ConnectionException(ERR_TIMED_OUT);
        } else {
            getClientIDFromServer();
        }
    }

    /**
     * Gets client ID from the server.
     *
     * @throws ConnectionException if kicked from the server
     * @throws RuntimeException    if interrupted when waiting for a ClientID message
     */
    private void getClientIDFromServer() {
        lastMessagesMutex.lock();
        if (lastMessages.size() == 0) {
            try {
                lastMessagesCondition.await();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted upon waiting for clientID message.");
            }
        }
        assert (lastMessages.size() != 0);
        ServerToClientMessage msg = lastMessages.peek();
        lastMessagesMutex.unlock();
        if (msg instanceof ServerKickMessage) {
            throw new ConnectionException(ERR_KICKED + ((ServerKickMessage) msg).getMessage());
        } else {
            assert msg instanceof ServerIDMessage;

            clientID = ((ServerIDMessage) msg).getID();
            playerNum = ((ServerIDMessage) msg).getPlayerNumber();
        }
    }


    /**
     * Reads a server to client message inside the buffer.
     * <p>
     * Assumes that client already has read enough bytes.
     *
     * @return read ServerToClientMessage
     */
    private ServerToClientMessage readServerToClientMessage() {
        assert (available() > 4);
        readBytesWithoutRemoval(sizeBuffer);
        assert (ByteBuffer.wrap(sizeBuffer).getInt() >= 0);
        int arrLength = ByteBuffer.wrap(sizeBuffer).getInt();
        int result = readInt();
        assert (result == arrLength);
        assert (available() >= arrLength);
        byte[] msgBuffer = readBytes(arrLength);
        try {
            ServerToClientMessage scm = (ServerToClientMessage) NetworkMessage.packetFromByteArray(msgBuffer);
            if (scm instanceof ServerKickMessage) {
                if (ClientFrame.useConsole)
                    Console.getConsole().addMessage("Received kick message from server: " + ((ServerKickMessage) scm).getMessage());
            }
            return scm;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Are you sure this is an actual server?");
            throw new RuntimeException(e.getMessage());
        }
    }

    // MODIFIES: this
    // EFFECTS: writes out a ClientToServerMessage to the server
    public void write(ClientToServerMessage msg) {
        try {
            byte[] bytes = NetworkMessage.packetToByteArray(msg);
            writeInt(bytes.length);
            writeNoLength(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // EFFECTS: reads the next four bytes from the server and interprets it as an int
    private int readInt() {
        byte[] bytes = readBytes(4);
        return ByteBuffer.wrap(bytes).getInt();
    }

    // EFFECTS: writes out the byte representation of an integer to the server
    private void writeInt(int a) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(a).array();
        writeNoLength(bytes);
    }

    // EFFECTS: gets current client ID
    public String getClientID() {
        return clientID;
    }

    public ServerToClientMessage removeServerToClientMessage() {
        lastMessagesMutex.lock();
        ServerToClientMessage message = lastMessages.remove();
        lastMessagesMutex.unlock();
        return message;
    }

    public int numMessages() {
        return lastMessages.size();
    }

    private final byte[] sizeBuffer = new byte[4];

    @Override
    public void dataReceivedEvent(ModifiedClient c) {
        int result = readBytesWithoutRemoval(sizeBuffer);
        if (result == 4) {
            int size = ByteBuffer.wrap(sizeBuffer).getInt();
            System.out.println("Data received! Amount available: " + available() + ", amount required: " + (size + 4));
            if (available() >= size + 4) {
                ServerToClientMessage message = readServerToClientMessage();
                lastMessagesMutex.lock();
                lastMessages.add(message);
                lastMessagesCondition.signal();
                lastMessagesMutex.unlock();
                objectEventReceiver.dataReceivedEvent(this, message);
            }
        }
    }

    @Override
    // MODIFIES: this
    // EFFECTS: disconnects client from the server and stops the client.
    public void stop() {
        lastMessagesMutex.lock();
        if (lastMessages.size() > 0) {
            lastMessages.removeIf(serverToClientMessage -> !(serverToClientMessage instanceof ServerKickMessage));
            if (lastMessages.size() > 0) {
                ServerToClientMessage message = lastMessages.poll();
                assert (message != null);
                if (message instanceof ServerKickMessage)
                    ClientFrame.getFrame().updateErrorMessage(((ServerKickMessage) message).getMessage());
                else
                    ClientFrame.getFrame().updateErrorMessage("You got kicked from the server. Kick message not received, however.");
                lastMessages.clear();
            }
        }
        super.stop();
    }

    public int getPlayerNum() {
        return playerNum;
    }
}
