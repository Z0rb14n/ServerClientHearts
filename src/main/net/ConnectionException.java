package net;

// Represents an exception thrown when you cannot connect.
public class ConnectionException extends RuntimeException {
    // EFFECTS: initializes exception with given message
    ConnectionException(String msg) {
        super(msg);
    }
}
