package client.console.command;

import org.jetbrains.annotations.Contract;

/**
 * Represents a command to be run
 */
public interface Command {

    /**
     * Executes the command
     */
    @Contract(pure = true)
    void execute();

    static Command constructCommand(String cmd) throws InvalidCommandException {
        if (PlayCommand.commandMatches(cmd)) return new PlayCommand(cmd);
        if (ChatCommand.commandMatches(cmd)) return new ChatCommand(cmd);
        if (ConnectCommand.commandMatches(cmd)) return new ConnectCommand(cmd);
        throw new InvalidCommandException("Command does not match any existing types: " + cmd);
    }
}
