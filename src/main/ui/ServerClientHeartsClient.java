package ui;

// TODO FINISH GUI AFTER FINALIZING SERVER

import net.ConnectionException;
import net.NewClient;
import processing.core.PApplet;
import processing.core.PImage;
import util.ClientState;

import java.awt.*;
import java.util.Scanner;

// Represents Client + GUI
public final class ServerClientHeartsClient extends PApplet {
    private final static int WHITE = 0xffffffff;
    private final static int BLACK = 0xff000000;
    private final static int RED = 0xffff0000;
    private final static String TOO_MANY_PLAYERS_MSG = "Too many players.";
    private final static String CONNECTION_TIMEOUT = "Timed out.";
    private final static String DEFAULT_COULD_NOT_CONNECT = "Could not connect.";
    private final static String INVALID_MSG = "Invalid message sent to server.";
    public static PImage CAT_DEFAULT;
    public static PImage CAT_FACE_LEFT;
    public static PImage CAT_FACE_RIGHT;
    public static PImage CAT_BACK_ONLY;
    public static PImage CAT_OUTLINE;
    public final static String DEFAULT_CAT_FILE = "./data/Symmetrical Miaow.png";
    public final static String CAT_LEFT_FILE = "./data/Symmetrical Miaow Face Left.png";
    public final static String CAT_RIGHT_FILE = "./data/Symmetrical Miaow Face Right.png";
    public final static String CAT_BACK_FILE = "./data/Symmetrical Miaow Background.png";
    public final static String CAT_OUTLINE_FILE = "./data/Symmetrical Miaow Outline.png";
    private final int CHAT_GREY = color(150);
    private final int CHAT_INACTIVE_GREY = color(120);
    private final int CHAT_DARK_GREY = color(50);
    private static final int CAT_WIDTH = 150;
    private static final int CAT_HEIGHT = 150;
    private final ServerClientHeartsClient actualClient = this;
    private final static int MAX_CHAT_MSG_LEN = 128;
    private ClientState clientState;
    private NewClient client;

    // Main function to run
    public static void main(String[] args) {
        ServerClientHeartsClient sch = new ServerClientHeartsClient();
        PApplet.runSketch(new String[]{"lmao"}/*Processing arguments*/, sch);
    }


    //<editor-fold desc="Initialization">

    @Override
    // MODIFIES: this
    // EFFECTS: sets size of window (see Processing for details)
    public void settings() {
        int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        size(screenWidth, screenHeight - 60);
    }

    @Override
    // MODIFIES: this
    // EFFECTS: initializes variables
    public void setup() {
        initCats();
        clientState = new ClientState();
        frameRate(30);
        surface.setTitle("Server Hearts Client!");
        TerminalMessageSender tms = new TerminalMessageSender();
        tms.start();
    }

    // MODIFIES: this
    // EFFECTS: initializes the cat images
    public void initCats() {
        CAT_DEFAULT = loadImage("./data/Symmetrical Miaow.png");
        CAT_FACE_LEFT = loadImage("./data/Symmetrical Miaow Face Left.png");
        CAT_FACE_RIGHT = loadImage("./data/Symmetrical Miaow Face Right.png");
        CAT_BACK_ONLY = loadImage("./data/Symmetrical Miaow Background.png");
        CAT_OUTLINE = loadImage("./data/Symmetrical Miaow Outline.png");
        CAT_DEFAULT.resize(CAT_WIDTH, CAT_HEIGHT);
        CAT_FACE_LEFT.resize(CAT_WIDTH, CAT_HEIGHT);
        CAT_FACE_RIGHT.resize(CAT_WIDTH, CAT_HEIGHT);
        CAT_BACK_ONLY.resize(CAT_WIDTH, CAT_HEIGHT);
        CAT_OUTLINE.resize(CAT_WIDTH, CAT_HEIGHT);
    }

    //</editor-fold>

    // EFFECTS: determines whether the client is active
    private boolean isClientInactive() {
        return client == null || !client.active();
    }

    //<editor-fold desc="Opening Menu">

    private final static float IP_ENTER_WIDTH = 225;
    private final static float IP_ENTER_HEIGHT = 30;
    private final static float IP_ENTER_BOX_X = (float) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2.0f - IP_ENTER_WIDTH / 2.0f;
    private final static float IP_ENTER_BOX_Y = 100;

    private static String errorDisplayed = "";

    // MODIFIES: this
    // EFFECTS: attempts to load the client
    public void tryLoadClient() {
        errorDisplayed = "";
        boolean failed = false;
        try {
            Thread thread = new Thread(() -> client = new NewClient(actualClient, ip));
            thread.start();
            final long time = System.nanoTime();
            thread.join(10000);

            if (System.nanoTime() - time > (9000000) && client == null) {
                failed = true;
                // thread.interrupt();   // ??? do I need this?
                errorDisplayed = CONNECTION_TIMEOUT;
            }
            if (client != null && client.actuallyInitialized) {
                clientState.setPlayerNum(client.getPlayerNum());
            }
        } catch (ConnectionException e) {
            System.out.print("HI");
            if (NewClient.TOO_MANY_PLAYERS.equals(e.getMessage())) {
                errorDisplayed = TOO_MANY_PLAYERS_MSG;
            }
            failed = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(2);
        }
        if (client == null) {
            if ("".equals(errorDisplayed)) {
                errorDisplayed = DEFAULT_COULD_NOT_CONNECT;
            }
        } else if (!failed) {
            errorDisplayed = "";
        }
    }

    private int ipEnterPosition = 0;
    private String ip = "";

    // MODIFIES: this
    // EFFECTS: draws the IP enter text
    private void drawIPEnterText() {
        fill(BLACK);
        textAlign(CENTER, CENTER);
        textSize(24);
        text("Enter IP", IP_ENTER_BOX_X + IP_ENTER_WIDTH / 2, IP_ENTER_BOX_Y - IP_ENTER_HEIGHT);
    }

    // MODIFIES: this
    // EFFECTS: drops the IP enter box
    private void drawIPEnterBox() {
        fill(WHITE);
        stroke(BLACK);
        strokeWeight(3);
        rect(IP_ENTER_BOX_X, IP_ENTER_BOX_Y, IP_ENTER_WIDTH, IP_ENTER_HEIGHT);
        fill(BLACK);
        textAlign(LEFT, TOP);
        textSize(24);
        String displayedIP = ip;
        while (textWidth(displayedIP) > IP_ENTER_WIDTH) {
            displayedIP = displayedIP.substring(0, displayedIP.length() - 1);
        }
        text(displayedIP, IP_ENTER_BOX_X + 2, IP_ENTER_BOX_Y);
        if (textWidth(ip.substring(0, ipEnterPosition)) <= IP_ENTER_WIDTH && frameCount % 30 < 15) {
            fill(BLACK);
            noStroke();
            float xpos = IP_ENTER_BOX_X;
            if (ipEnterPosition != 0) {
                xpos += textWidth(ip.substring(0, ipEnterPosition));
            } else {
                xpos += 2;
            }
            rect(xpos, IP_ENTER_BOX_Y + 2, 2, IP_ENTER_HEIGHT - 4);
        }
        if (!"".equals(errorDisplayed)) {
            textAlign(CENTER, CENTER);
            textSize(12);
            fill(RED);
            text(errorDisplayed, IP_ENTER_BOX_X + IP_ENTER_WIDTH / 2, IP_ENTER_BOX_Y + IP_ENTER_HEIGHT + 20);
        }
    }

    // MODIFIES: this
    // EFFECTS: add/removes/returns characters for the IP Enter Box
    private void onKeyIPEnter() {
        if (key != CODED && key != BACKSPACE && key != RETURN && key != ENTER && key != TAB && key != ESC) {
            ip = ip.substring(0, ipEnterPosition) + key + ip.substring(ipEnterPosition);
            ipEnterPosition++;
        } else if (key == BACKSPACE) {
            if (ip.length() != 0 && ipEnterPosition != 0) {
                ip = ip.substring(0, ipEnterPosition - 1) + ip.substring(ipEnterPosition);
                ipEnterPosition--;
            }
        } else if (keyCode == LEFT) {
            if (ipEnterPosition > 0) {
                ipEnterPosition--;
            }
        } else if (keyCode == RIGHT) {
            if (ipEnterPosition < ip.length()) {
                ipEnterPosition++;
            }
        } else if (key == RETURN || key == ENTER) {
            tryLoadClient();
        }
    }

    // MODIFIES: this
    // EFFECTS: gets the CURRENT_PLAYERS message if the client accidentally gets it
    public void catchAccidentalCurrentPlayersMessage(String msg) {
        System.out.println("Accidental CurrentPlayers message found in ID string: " + msg);
        clientState.processNewMessage(msg);
    }

    //</editor-fold>

    @Override
    // MODIFIES: this
    // EFFECTS: code run when a key is pressed (see Processing)
    public void keyPressed() {
        if (isClientInactive()) {
            onKeyIPEnter();
        } else {
            if (isChatActive) {
                onKeyChatActive();
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: updates error message with given message
    public void updateErrorMessage(String msg) {
        if (msg.equals(NewClient.ERR_INVALID_MSG)) {
            errorDisplayed = INVALID_MSG;
        } else {
            errorDisplayed = msg;
        }

    }

    @Override
    // MODIFIES: this
    // EFFECTS: code run when mouse button is pressed
    public void mousePressed() {
        if (mouseHeldDown) return;
        mouseHeldDown = true;
        mouseFirstPressed();
    }

    // MODIFIES: this
    // EFFECTS: code run at the instant the mouse button is pressed, but not afterwards
    private void mouseFirstPressed() {
        updateChatWindowActivity();
    }

    @Override
    // MODIFIES: this
    // EFFECTS: code run when the mouse is released - detects when the mouse is held down
    public void mouseReleased() {
        mouseHeldDown = false;
    }

    //<editor-fold desc="Chat window">
    private boolean isChatActive = true;

    private final static float outerChatWindowX = 850;
    private final static float outerChatWindowY = 0;
    private final static float outerChatWindowWidth = 250;
    private final static float outerChatWindowHeight = 600;

    private final static float innerChatWindowWidth = outerChatWindowWidth - (10 * 2);
    private final static float innerChatWindowHeight = 25;
    private final static float innerChatWindowX = outerChatWindowX + 10;
    private final static float innerChatWindowY = outerChatWindowY + outerChatWindowHeight - innerChatWindowHeight - 20;

    private boolean mouseHeldDown = false;

    private String newChatMessage = "";
    private int chatWindowIndexPosition = 0;

    private int firstVisibleChar = 0;
    private int lastVisibleChar = 0;
    private int bottomLineNum = 0;

    // You could write the chat window as a PGraphics object and simply call image(PGraphics, x, y);, but that's big brain

    // MODIFIES: this
    // EFFECTS: updates whether the chat window is active
    private void updateChatWindowActivity() {
        if (isChatActive) {
            if ((mouseX < outerChatWindowX || mouseX > outerChatWindowX + outerChatWindowWidth) || (mouseY < outerChatWindowY || mouseY > outerChatWindowY + outerChatWindowHeight)) {
                isChatActive = false;
            }
        } else {
            if (mouseX > innerChatWindowX && mouseX < innerChatWindowX + innerChatWindowWidth && mouseY > innerChatWindowY && mouseY < innerChatWindowHeight + mouseY) {
                isChatActive = true;
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: adjusts values of first visible and last visible character
    private void onKeyAdjustVisibleCharacters() {
        // bounds check
        if (chatWindowIndexPosition < firstVisibleChar) {
            firstVisibleChar = chatWindowIndexPosition;
            while (lastVisibleChar > newChatMessage.length() || (textWidth(newChatMessage.substring(firstVisibleChar, lastVisibleChar)) > innerChatWindowWidth)) {
                lastVisibleChar--;
            }
        }
        if (chatWindowIndexPosition > lastVisibleChar) {
            lastVisibleChar = chatWindowIndexPosition;
            while (textWidth(newChatMessage.substring(firstVisibleChar, lastVisibleChar)) > innerChatWindowWidth) {
                firstVisibleChar++;
            }
        }
        if (lastVisibleChar > newChatMessage.length()) {
            lastVisibleChar = newChatMessage.length();
            while (textWidth(newChatMessage.substring(firstVisibleChar, lastVisibleChar)) > innerChatWindowWidth) {
                firstVisibleChar++;
            }
        }
    }

    // MODIFIES; this
    // EFFECTS: code that is run when user types a key when chat window is active
    private void onKeyChatActive() {
        if (key != CODED && key != BACKSPACE && key != RETURN && key != ENTER && key != TAB && key != ESC) {
            if (newChatMessage.length() < MAX_CHAT_MSG_LEN) {
                newChatMessage = newChatMessage.substring(0, chatWindowIndexPosition) + key + newChatMessage.substring(chatWindowIndexPosition);
                chatWindowIndexPosition++;
                onKeyAdjustVisibleCharacters();
            }
        } else if (key == BACKSPACE) {
            if (newChatMessage.length() != 0 && chatWindowIndexPosition != 0) {
                newChatMessage = newChatMessage.substring(0, chatWindowIndexPosition - 1) + newChatMessage.substring(chatWindowIndexPosition);
                chatWindowIndexPosition--;
                onKeyAdjustVisibleCharacters();
            }
        } else if (keyCode == LEFT) {
            if (chatWindowIndexPosition > 0) {
                chatWindowIndexPosition--;
                onKeyAdjustVisibleCharacters();
            }
        } else if (keyCode == RIGHT) {
            if (chatWindowIndexPosition < newChatMessage.length()) {
                chatWindowIndexPosition++;
                onKeyAdjustVisibleCharacters();
            }
        } else if (key == RETURN || key == ENTER) {
            if (newChatMessage.length() != 0) {
                client.sendChatMessage(newChatMessage);
                newChatMessage = "";
                chatWindowIndexPosition = 0;
                lastVisibleChar = 0;
                firstVisibleChar = 0;
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: draws the chat window
    private void drawChatWindow() {
        noStroke();
        fill(CHAT_GREY);
        rect(outerChatWindowX, outerChatWindowY, outerChatWindowWidth, outerChatWindowHeight);
        if (isChatActive) {
            fill(CHAT_DARK_GREY);
        } else {
            fill(CHAT_INACTIVE_GREY);
        }
        textSize(innerChatWindowHeight - 4);
        rect(innerChatWindowX, innerChatWindowY, innerChatWindowWidth, innerChatWindowHeight);
        textAlign(LEFT, TOP);
        fill(WHITE);
        if (newChatMessage.length() != 0)
            text(newChatMessage.substring(firstVisibleChar, lastVisibleChar), innerChatWindowX + 2, innerChatWindowY);
        if (frameCount % 30 < 15) drawChatCursor();
    }

    // MODIFIES: this
    // EFFECTS: draws the chat cursor
    private void drawChatCursor() {
        fill(WHITE);
        noStroke();
        if (firstVisibleChar == lastVisibleChar) {
            rect(innerChatWindowX + 2, innerChatWindowY + 2, 2, innerChatWindowHeight - 4);
        } else {
            rect(innerChatWindowX + textWidth(newChatMessage.substring(firstVisibleChar, chatWindowIndexPosition)), innerChatWindowY + 2, 2, innerChatWindowHeight - 4);
        }
    }
    //</editor-fold>

    @Override
    // MODIFIES: this
    // EFFECTS: runs FPS times a second to draw to a screen
    public void draw() {
        background(WHITE);
        if (isClientInactive()) {
            drawIPEnterText();
            drawIPEnterBox();
        } else {
            drawChatWindow();
            if (client.available() > 0) {
                String clientMessage = client.readString();
                clientState.processNewMessage(clientMessage);
                System.out.println("New Message from Server: " + clientMessage);

            }
            if (clientState.getPlayerNum() == 1) {
                fill(RED);
                textAlign(CENTER);
                text("YOU", 375, 80);
            }
            fill(RED);
            textAlign(CENTER);
            switch (clientState.getPlayerNum()) {
                case 1:
                    text("YOU", 375, 80);
                    break;
                case 2:
                    text("YOU", 675, 250);
                    break;
                case 3:
                    text("YOU", 375, 350);
                    break;
                case 4:
                    text("YOU", 105, 250);
                    break;
                default:
                    text("Umm...", (float) width / 2, (float) height / 2);
            }
            image(clientState.getDrawnImages()[0], 300, 80);
            image(clientState.getDrawnImages()[3], 30, 250);
            image(clientState.getDrawnImages()[1], 600, 250);
            image(clientState.getDrawnImages()[2], 300, 350);
        }
    }

    // Represents the Terminal I/O that can communicate with the server
    private class TerminalMessageSender extends Thread {
        @Override
        // MODIFIES: this
        // EFFECTS: when there's contents of the terminal to write, write it to the server
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String lol = scanner.nextLine();
                if (client != null) {
                    client.write(lol);
                }
            }
        }
    }
}
