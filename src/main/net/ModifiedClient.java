package net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * Processing's Client class but modified to not use PApplet
 */
public class ModifiedClient implements Runnable {

    protected static final int MAX_BUFFER_SIZE = 1 << 27; // 128 MB

    protected EventReceiver eventReceiver;

    protected volatile Thread thread;
    protected Socket socket;

    protected InputStream input;
    protected OutputStream output;

    protected final Object bufferLock = new Object[0];

    protected byte[] buffer = new byte[32768];
    protected int bufferIndex;
    protected int bufferLast;


    /**
     * @param parent typically use "this"
     * @param host   address of the server
     * @param port   port to read/write from on the server
     */
    public ModifiedClient(EventReceiver parent, String host, int port) {
        this.eventReceiver = parent;

        try {
            socket = new Socket(host, port);
            input = socket.getInputStream();
            output = socket.getOutputStream();

            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
            dispose();
        }
    }

    /**
     * @param socket any object of type Socket
     * @throws IOException thrown on failure to get input/output streams
     */
    public ModifiedClient(EventReceiver parent, Socket socket) throws IOException {
        this.eventReceiver = parent;
        this.socket = socket;

        input = socket.getInputStream();
        output = socket.getOutputStream();

        thread = new Thread(this);
        thread.start();
    }

    /**
     * Disconnects from the server - use to shut the connection when you're
     * finished with the Client.
     */
    public void stop() {
        if (thread != null && eventReceiver != null) {
            eventReceiver.disconnectEvent(this);
        }
        dispose();
    }

    public void setEventReceiver(EventReceiver er) {
        this.eventReceiver = er;
    }


    /**
     * Disconnect from the server, but does not trigger a disconnect event.
     */
    protected void dispose() {
        thread = null;
        try {
            if (input != null) {
                input.close();
                input = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (output != null) {
                output.close();
                output = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        byte[] readBuffer;
        { // make the read buffer same size as socket receive buffer so that
            // we don't waste cycles calling listeners when there is more data waiting
            int readBufferSize = 1 << 16; // 64 KB (default socket receive buffer size)
            try {
                readBufferSize = socket.getReceiveBufferSize();
            } catch (SocketException ignore) {
            }
            readBuffer = new byte[readBufferSize];
        }
        while (Thread.currentThread() == thread) {
            try {
                while (input != null) {
                    int readCount;

                    // try to read a byte using a blocking read.
                    // An exception will occur when the sketch is exits.
                    try {
                        readCount = input.read(readBuffer, 0, readBuffer.length);
                    } catch (SocketException e) {
                        System.err.println("Client SocketException: " + e.getMessage());
                        // the socket had a problem reading so don't try to read from it again.
                        stop();
                        return;
                    }

                    // read returns -1 if end-of-stream occurs (for example if the host disappears)
                    if (readCount == -1) {
                        System.err.println("Client got end-of-stream.");
                        if (eventReceiver != null) eventReceiver.endOfStreamEvent(this);
                        stop();
                        return;
                    }

                    synchronized (bufferLock) {
                        int freeBack = buffer.length - bufferLast;
                        if (readCount > freeBack) {
                            // not enough space at the back
                            int bufferLength = bufferLast - bufferIndex;
                            byte[] targetBuffer = buffer;
                            if (bufferLength + readCount > buffer.length) {
                                // can't fit even after compacting, resize the buffer
                                // find the next power of two which can fit everything in
                                int newSize = Integer.highestOneBit(bufferLength + readCount - 1) << 1;
                                if (newSize > MAX_BUFFER_SIZE) {
                                    // buffer is full because client is not reading (fast enough)
                                    System.err.println("Client: can't receive more data, buffer is full. " +
                                            "Make sure you read the data from the client.");
                                    stop();
                                    return;
                                }
                                targetBuffer = new byte[newSize];
                            }
                            // compact the buffer (either in-place or into the new bigger buffer)
                            System.arraycopy(buffer, bufferIndex, targetBuffer, 0, bufferLength);
                            bufferLast -= bufferIndex;
                            bufferIndex = 0;
                            buffer = targetBuffer;
                        }
                        // copy all newly read bytes into the buffer
                        System.arraycopy(readBuffer, 0, buffer, bufferLast, readCount);
                        bufferLast += readCount;
                    }

                    // now post an event
                    if (eventReceiver != null) eventReceiver.dataReceivedEvent(this);
                }
            } catch (IOException e) {
                //errorMessage("run", e);
                e.printStackTrace();
            }
        }
    }


    /**
     * Returns true if this client is still active and hasn't run
     * into any trouble.
     *
     * @return true if this client is still active and hasn't run into any trouble.
     */
    public boolean active() {
        return (thread != null);
    }


    /**
     * Returns the IP address of the computer to which the Client is attached.
     *
     * @return string containing IP address of the computer to which the Client is attached.
     */
    public String ip() {
        if (socket != null) {
            return socket.getInetAddress().getHostAddress();
        }
        return null;
    }


    /**
     * Returns the number of bytes available. When any client has bytes
     * available from the server, it returns the number of bytes.
     *
     * @return number of bytes available
     */
    public int available() {
        synchronized (bufferLock) {
            return (bufferLast - bufferIndex);
        }
    }


    /**
     * Empty the buffer - removes all the data stored there.
     */
    public void clear() {
        synchronized (bufferLock) {
            bufferLast = 0;
            bufferIndex = 0;
        }
    }


    /**
     * Return a byte array of anything that's in the serial buffer
     * up to the specified maximum number of bytes.
     * Not particularly memory/speed efficient, because it creates
     * a byte array on each read, but it's easier to use than
     * readBytes(byte b[]).
     *
     * @param max the maximum number of bytes to read
     * @return read bytes
     */
    public byte[] readBytes(int max) {
        synchronized (bufferLock) {
            if (bufferIndex == bufferLast) return null;

            int length = bufferLast - bufferIndex;
            if (length > max) length = max;
            byte[] outgoing = new byte[length];
            System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

            bufferIndex += length;
            if (bufferIndex == bufferLast) {
                bufferIndex = 0;  // rewind
                bufferLast = 0;
            }

            return outgoing;
        }
    }


    /**
     * Grab whatever is in the serial buffer, and stuff it into a
     * byte buffer passed in by the user. This is more memory/time
     * efficient than readBytes() returning a byte[] array.
     * <p>
     * Returns an int for how many bytes were read. If more bytes
     * are available than can fit into the byte array, only those
     * that will fit are read.
     *
     * @param bytebuffer passed in byte array to be altered
     * @return how many bytes were read
     */
    public int readBytes(byte[] bytebuffer) {
        synchronized (bufferLock) {
            if (bufferIndex == bufferLast) return 0;

            int length = bufferLast - bufferIndex;
            if (length > bytebuffer.length) length = bytebuffer.length;
            System.arraycopy(buffer, bufferIndex, bytebuffer, 0, length);

            bufferIndex += length;
            if (bufferIndex == bufferLast) {
                bufferIndex = 0;  // rewind
                bufferLast = 0;
            }
            return length;
        }
    }

    /**
     * Grab whatever is in the serial buffer, and stuff it into a
     * byte buffer passed in by the user. This is more memory/time
     * efficient than readBytes() returning a byte[] array.
     * <p>
     * Returns an int for how many bytes were read. If more bytes
     * are available than can fit into the byte array, only those
     * that will fit are read.
     *
     * @param bytebuffer passed in byte array to be altered
     * @return how many bytes were read
     */
    public int readBytesWithoutRemoval(byte[] bytebuffer) {
        synchronized (bufferLock) {
            if (bufferIndex == bufferLast) return 0;

            int length = bufferLast - bufferIndex;
            if (length > bytebuffer.length) length = bytebuffer.length;
            System.arraycopy(buffer, bufferIndex, bytebuffer, 0, length);
            return length;
        }
    }

    public void writeNoLength(byte[] data) {
        try {
            output.write(data);
            output.flush();   // hmm, not sure if a good idea
        } catch (Exception e) { // null pointer or serial port dead
            //errorMessage("write", e);
            //e.printStackTrace();
            //disconnect(e);
            e.printStackTrace();
            stop();
        }
    }

    public void write(byte[] data) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(data.length);
            output.write(buffer.array());
            output.write(data);
            output.flush();   // hmm, not sure if a good idea
            System.out.println("Writing data of length " + data.length);
        } catch (Exception e) { // null pointer or serial port dead
            //errorMessage("write", e);
            //e.printStackTrace();
            //disconnect(e);
            e.printStackTrace();
            stop();
        }
    }
}
