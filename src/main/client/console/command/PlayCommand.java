package client.console.command;

import client.GameClient;
import client.console.Console;
import org.jetbrains.annotations.NotNull;
import util.card.Card;
import util.card.Deck;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Command that can play 3 or 1 cards
 */
public class PlayCommand implements Command {
    private final static Pattern PLAY_PATTERN = Pattern.compile("play .+");
    private final static int PLAY_MSG_SUBSTRING = "play ".length();
    @NotNull
    private final Deck cards = new Deck();

    /**
     * Constructs a play command given string of full command
     *
     * @param cmdText entire command text
     * @throws InvalidCommandException if command does not match a play command
     * @throws InvalidCommandException if command contains invalid cards
     * @throws InvalidCommandException if number of cards is not 3 or 1
     */
    public PlayCommand(@NotNull String cmdText) throws InvalidCommandException {
        if (!PLAY_PATTERN.matcher(cmdText).matches())
            throw new InvalidCommandException("Command is not PlayCommand: " + cmdText);
        String cardInput = cmdText.substring(PLAY_MSG_SUBSTRING);
        try (Scanner scanner = new Scanner(cardInput)) {
            String str;
            while (scanner.hasNext()) {
                str = scanner.next();
                if (str.isEmpty()) break;
                cards.add(new Card(str));
            }
        } catch (IllegalArgumentException e) {
            Console.getConsole().addMessage("Invalid arguments in PlayCommand: " + cmdText);
            throw new InvalidCommandException("Invalid Arguments in PlayCommand: " + cmdText);
        }
        if (cards.size() != 3 && cards.size() != 1)
            throw new InvalidCommandException("Invalid number of cards in PlayCommand: " + cmdText);
    }

    @Override
    public void execute() {
        if (cards.size() == 3) {
            GameClient.getInstance().passCards(cards.get(0), cards.get(1), cards.get(2));
        } else {
            GameClient.getInstance().playCard(cards.get(0));
        }
    }

    /**
     * Checks if given command string matches the play command pattern
     *
     * @param str command string
     * @return true if string matches given command string
     */
    public static boolean commandMatches(String str) {
        return PLAY_PATTERN.matcher(str).matches();
    }
}
