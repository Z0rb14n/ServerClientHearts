package net;

// Represents an exception thrown when you cannot connect.
public class ConnectionException extends RuntimeException {

    // EFFECTS: initializes exception with given message
    public ConnectionException(String msg) {
        super(msg);
    }

    // EFFECTS: initializes exception with null message
    public ConnectionException() {
        super();
    }
}
