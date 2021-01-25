package net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Processing's Client class but modified to not use PApplet
 */
public class ModifiedClient implements Runnable {

    private static final int MAX_BUFFER_SIZE = 1 << 27; // 128 MB

    private final EventReceiver eventReceiver;

    private volatile Thread thread;
    private Socket socket;

    private InputStream input;
    private OutputStream output;

    private final Object bufferLock = new Object[0];

    private byte[] buffer = new byte[32768];
    private int bufferIndex;
    private int bufferLast;


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
        if (thread != null) {
            eventReceiver.disconnectEvent(this);
        }
        dispose();
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
                        eventReceiver.endOfStreamEvent(this);
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
                    eventReceiver.dataReceivedEvent(this);
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
     * Returns a number between 0 and 255 for the next byte that's waiting in
     * the buffer. Returns -1 if there is no byte, although this should be
     * avoided by first checking <b>available()</b> to see if any data is available.
     */
    public int read() {
        synchronized (bufferLock) {
            if (bufferIndex == bufferLast) return -1;

            int outgoing = buffer[bufferIndex++] & 0xff;
            if (bufferIndex == bufferLast) {  // rewind
                bufferIndex = 0;
                bufferLast = 0;
            }
            return outgoing;
        }
    }


    /**
     * Returns the next byte in the buffer as a char. Returns -1 or 0xffff if
     * nothing is there.
     *
     * @return next byte in buffer as a char, or 0xffff if none is there.
     */
    public char readChar() {
        synchronized (bufferLock) {
            if (bufferIndex == bufferLast) return (char) (-1);
            return (char) read();
        }
    }


    /**
     * Reads a group of bytes from the buffer. The version with no parameters
     * returns a byte array of all data in the buffer. This is not efficient,
     * but is easy to use. The version with the <b>byteBuffer</b> parameter is
     * more memory and time efficient. It grabs the data in the buffer and puts
     * it into the byte array passed in and returns an int value for the number
     * of bytes read. If more bytes are available than can fit into the
     * <b>byteBuffer</b>, only those that fit are read.
     *
     * Return a byte array of anything that's in the serial buffer.
     * Not particularly memory/speed efficient, because it creates
     * a byte array on each read, but it's easier to use than
     * readBytes(byte b[]).
     *
     * @return read bytes
     */
    public byte[] readBytes() {
        synchronized (bufferLock) {
            if (bufferIndex == bufferLast) return null;

            int length = bufferLast - bufferIndex;
            byte[] outgoing = new byte[length];
            System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

            bufferIndex = 0;  // rewind
            bufferLast = 0;
            return outgoing;
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
     * Reads from the port into a buffer of bytes up to and including a
     * particular character. If the character isn't in the buffer, 'null' is
     * returned. The version with no <b>byteBuffer</b> parameter returns a byte
     * array of all data up to and including the <b>interesting</b> byte. This
     * is not efficient, but is easy to use. The version with the
     * <b>byteBuffer</b> parameter is more memory and time efficient. It grabs
     * the data in the buffer and puts it into the byte array passed in and
     * returns an int value for the number of bytes read. If the byte buffer is
     * not large enough, -1 is returned and an error is printed to the message
     * area. If nothing is in the buffer, 0 is returned.
     *
     * @param interesting character designated to mark the end of the data
     * @return read bytes
     */
    public byte[] readBytesUntil(int interesting) {
        byte what = (byte) interesting;

        synchronized (bufferLock) {
            if (bufferIndex == bufferLast) return null;

            int found = -1;
            for (int k = bufferIndex; k < bufferLast; k++) {
                if (buffer[k] == what) {
                    found = k;
                    break;
                }
            }
            if (found == -1) return null;

            int length = found - bufferIndex + 1;
            byte[] outgoing = new byte[length];
            System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

            bufferIndex += length;
            if (bufferIndex == bufferLast) {
                bufferIndex = 0; // rewind
                bufferLast = 0;
            }
            return outgoing;
        }
    }


    /**
     * Reads from the serial port into a buffer of bytes until a
     * particular character. If the character isn't in the serial
     * buffer, then 'null' is returned.
     *
     * If outgoing[] is not big enough, then -1 is returned,
     * and an error message is printed on the console.
     * If nothing is in the buffer, zero is returned.
     * If 'interesting' byte is not in the buffer, then 0 is returned.
     *
     * @param byteBuffer passed in byte array to be altered
     * @return number of bytes read
     */
    public int readBytesUntil(int interesting, byte[] byteBuffer) {
        byte what = (byte) interesting;

        synchronized (bufferLock) {
            if (bufferIndex == bufferLast) return 0;

            int found = -1;
            for (int k = bufferIndex; k < bufferLast; k++) {
                if (buffer[k] == what) {
                    found = k;
                    break;
                }
            }
            if (found == -1) return 0;

            int length = found - bufferIndex + 1;
            if (length > byteBuffer.length) {
                System.err.println("readBytesUntil() byte buffer is" +
                        " too small for the " + length +
                        " bytes up to and including char " + interesting);
                return -1;
            }
            //byte outgoing[] = new byte[length];
            System.arraycopy(buffer, bufferIndex, byteBuffer, 0, length);

            bufferIndex += length;
            if (bufferIndex == bufferLast) {
                bufferIndex = 0;  // rewind
                bufferLast = 0;
            }
            return length;
        }
    }


    /**
     * Returns the all the data from the buffer as a String. This method
     * assumes the incoming characters are ASCII. If you want to transfer
     * Unicode data, first convert the String to a byte stream in the
     * representation of your choice (i.e. UTF8 or two-byte Unicode data), and
     * send it as a byte array.
     *
     * @return bytes in the buffer read as an ASCII string
     */
    public String readString() {
        byte[] b = readBytes();
        if (b == null) return null;
        return new String(b);
    }


    /**
     * Combination of <b>readBytesUntil()</b> and <b>readString()</b>. Returns
     * <b>null</b> if it doesn't find what you're looking for.
     * <p>
     * If you want to move Unicode data, you can first convert the
     * String to a byte stream in the representation of your choice
     * (i.e. UTF8 or two-byte Unicode data), and send it as a byte array.
     *
     * @param interesting character designated to mark the end of the data
     * @return buffer as string until interesting character is found, or null if it can't find it.
     */
    public String readStringUntil(int interesting) {
        byte[] b = readBytesUntil(interesting);
        if (b == null) return null;
        return new String(b);
    }


    /**
     * Writes data to a server specified when constructing the client.
     *
     * @param data data to write - can also be a char
     */
    public void write(int data) {
        try {
            output.write(data & 0xff);   // for good measure do the &
            output.flush();                 // hmm, not sure if a good idea

        } catch (Exception e) { // null pointer or serial port dead
            //errorMessage("write", e);
            //e.printStackTrace();
            //dispose();
            //disconnect(e);
            e.printStackTrace();
            stop();
        }
    }


    public void write(byte[] data) {
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


    /**
     * Write a String to the output. Note that this doesn't account
     * for Unicode (two bytes per char), nor will it send UTF8
     * characters.. It assumes that you mean to send a byte buffer
     * (most often the case for networking and serial i/o) and
     * will only use the bottom 8 bits of each char in the string.
     * (Meaning that internally it uses String.getBytes)
     *
     * If you want to move Unicode data, you can first convert the
     * String to a byte stream in the representation of your choice
     * (i.e. UTF8 or two-byte Unicode data), and send it as a byte array.
     */
    public void write(String data) {
        write(data.getBytes());
    }
}
