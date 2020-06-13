package net;

// Represents an exception thrown when you cannot connect.
public class ConnectionException extends RuntimeException {
    // EFFECTS: initializes ConnectionException with given error message
    public ConnectionException(String msg) {
        super(msg);
    }

    // EFFECTS: initializes ConnectionException without a given error message
    public ConnectionException() {
        super();
    }
}
