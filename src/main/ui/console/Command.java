package ui.console;

import util.Card;
import util.Deck;
import util.GameClient;

import java.util.Scanner;

// Represents a command to be run
final class Command {
    private final static String CONNECT = "connect .+";
    private final static int CONNECT_IP_SUBSTRING = "connect ".length();

    private final static String CHAT = "say .+";
    private final static int CHAT_MSG_SUBSTRING = "say ".length();

    private final static String PLAY = "play .+";
    private final static int PLAY_MSG_SUBSTRING = "play ".length();
    private String ip;
    private String chatMsg;
    private Deck cards;

    // EFFECTS: initializes the command with given command input
    Command(String cmdInput) throws InvalidCommandException {
        if (cmdInput.matches(CONNECT)) ip = cmdInput.substring(CONNECT_IP_SUBSTRING);
        else if (cmdInput.matches(CHAT)) chatMsg = cmdInput.substring(CHAT_MSG_SUBSTRING);
        else if (cmdInput.matches(PLAY)) {
            Deck deck = new Deck();
            String cardInput = cmdInput.substring(PLAY_MSG_SUBSTRING);
            System.out.print(cardInput);
            try (Scanner scanner = new Scanner(cardInput)) {
                String str;
                while (scanner.hasNext()) {
                    str = scanner.next();
                    if (str.isEmpty()) break;
                    deck.add(new Card(str));
                }
                cards = deck;
            } catch (IllegalArgumentException e) {
                Console.getConsole().addMessage("Invalid arguments");
                throw new InvalidCommandException();
            }
        } else throw new InvalidCommandException();
    }

    // EFFECTS: runs the command (i.e. connection to server/chat message)
    void runCommand() {
        try {
            if (ip != null) {
                GameClient.getInstance().connect(ip);
            } else if (chatMsg != null) {
                GameClient.ClientLogger.logMessage("[Command]: Sending message " + chatMsg);
                GameClient.getInstance().sendChatMessage(chatMsg);
            } else if (cards != null) {
                if (cards.size() == 3) {
                    GameClient.getInstance().passCards(cards.get(0), cards.get(1), cards.get(2));
                } else {
                    Console.getConsole().addMessage("Invalid number of cards in passCards command");
                }
            }
        } catch (IllegalArgumentException e) {
            Console.getConsole().addMessage("Invalid arguments/command");
        }
    }
}
