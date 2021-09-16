package client.console.command;

import client.GameClient;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Command to send a chat message
 */
public class ChatCommand implements Command {
    private final static Pattern CHAT_PATTERN = Pattern.compile("say .+");
    private final static int CHAT_MSG_SUBSTRING = "say ".length();
    @NotNull
    private final String message;

    /**
     * Constructs a chat command given string of full command
     *
     * @param cmdText entire command text
     * @throws InvalidCommandException if command does not match a chat command
     */
    public ChatCommand(@NotNull String cmdText) throws InvalidCommandException {
        if (!CHAT_PATTERN.matcher(cmdText).matches())
            throw new InvalidCommandException("Command was not ChatCommand: " + cmdText);
        this.message = cmdText.substring(CHAT_MSG_SUBSTRING);
    }

    @Contract(pure = true)
    public void execute() {
        System.out.println("[Command]: Sending message " + message);
        GameClient.getInstance().sendChatMessage(message);
    }

    /**
     * Checks if given command string matches the chat command pattern
     *
     * @param str command string
     * @return true if string matches given command string
     */
    public static boolean commandMatches(String str) {
        return CHAT_PATTERN.matcher(str).matches();
    }
}
