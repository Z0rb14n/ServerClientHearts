package net;

public class ConnectionException extends RuntimeException {

    public ConnectionException(String msg) {
        super(msg);
    }

    public ConnectionException() {
        super();
    }
}
