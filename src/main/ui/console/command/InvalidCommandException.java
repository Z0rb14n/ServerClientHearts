package ui.console.command;

// Represents an exception thrown to indicate instantiating an invalid command
public class InvalidCommandException extends Exception {
    public InvalidCommandException() {
        super();
    }

    public InvalidCommandException(String s) {
        super(s);
    }
}
