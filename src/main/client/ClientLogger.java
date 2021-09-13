package client;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Logger for the game client
 */
public class ClientLogger {
    private static final StringBuilder stringBuilder = new StringBuilder();
    private static final ArrayList<String> messages = new ArrayList<>(100);

    /**
     * Gets the full list of messages
     *
     * @return unmodifiable list of messages
     */
    @Contract(pure = true)
    public static List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /**
     * Logs a single message to the ClientLogger
     *
     * @param message message to log
     */
    public static void logMessage(@NotNull String message) {
        stringBuilder.append(message).append("\n");
        messages.add(message);
        System.out.println(message);
    }

    /**
     * Gets the full logger text output
     *
     * @return full output
     */
    @Contract(pure = true)
    public static String getLoggerText() {
        return stringBuilder.toString();
    }
}
