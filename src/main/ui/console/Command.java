package ui.console;

import ui.client.MainFrame;
import util.Card;
import util.Deck;

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
        if (cmdInput.matches(CONNECT)) {
            ip = cmdInput.substring(CONNECT_IP_SUBSTRING);
        } else if (cmdInput.matches(CHAT)) {
            chatMsg = cmdInput.substring(CHAT_MSG_SUBSTRING);
        } else if (cmdInput.matches(PLAY)) {
            Deck deck = new Deck();
            String cardInput = cmdInput.substring(PLAY_MSG_SUBSTRING);
            Scanner scanner = new Scanner(cardInput);
            for (String card = scanner.next(); scanner.hasNext(); ) {
                try {
                    deck.addCard(new Card(card));
                } catch (IllegalArgumentException e) {
                    Console.getConsole().addMessage("Invalid command.");
                    throw new InvalidCommandException();
                }
            }
            cards = deck;
        } else {
            throw new InvalidCommandException();
        }
    }

    // EFFECTS: runs the command (i.e. connection to server/chat message)
    void runCommand() {
        if (ip != null) {
            MainFrame.getFrame().attemptLoadClient(ip);
        } else if (chatMsg != null) {
            MainFrame.getFrame().sendChatMessage(chatMsg);
        } else if (cards != null) {
            MainFrame.getFrame().playCards(cards);
        }
    }
}
