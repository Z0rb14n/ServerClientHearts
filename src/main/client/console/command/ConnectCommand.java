package client.console.command;

import client.GameClient;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Command that would connect to an IP address
 */
public class ConnectCommand implements Command {
    private final static Pattern CONNECT_PATTERN = Pattern.compile("connect .+");
    private final static int CONNECT_IP_SUBSTRING = "connect ".length();
    @NotNull
    private final String ip;

    /**
     * Constructs a connect command given string of full command
     *
     * @param cmdText entire command text
     * @throws InvalidCommandException if command does not match a connect command
     */
    public ConnectCommand(@NotNull String cmdText) throws InvalidCommandException {
        if (!CONNECT_PATTERN.matcher(cmdText).matches())
            throw new InvalidCommandException("Command was not a ConnectCommand: " + cmdText);
        this.ip = cmdText.substring(CONNECT_IP_SUBSTRING);
    }

    @Contract(pure = true)
    public void execute() {
        GameClient.getInstance().connect(ip);
    }

    /**
     * Checks if given command string matches the connect command pattern
     *
     * @param str command string
     * @return true if string matches given command string
     */
    public static boolean commandMatches(String str) {
        return CONNECT_PATTERN.matcher(str).matches();
    }
}
