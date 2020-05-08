package ui;

// TODO FINISH GUI AFTER FINALIZING SERVER

// TODO RE-FIX CHAT WINDOW AFTER... CERTAIN COMMITS BROKE IT

import net.ConnectionException;
import net.NewClient;
import net.ServerToClientMessage;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.event.MouseEvent;
import util.Card;
import util.ClientState;

import java.util.Iterator;
import java.util.LinkedList;

import static net.Constants.*;

// Represents Client + GUI
public final class ServerClientHeartsClient extends PApplet {
    private final static String TOO_MANY_PLAYERS_MSG = "Too many players.";
    private final static String CONNECTION_TIMEOUT = "Timed out.";
    private final static String DEFAULT_COULD_NOT_CONNECT = "Could not connect.";
    private final static String INVALID_MSG = "Invalid message sent to server.";
    public static PImage CAT_DEFAULT;
    public static PImage CAT_FACE_LEFT;
    public static PImage CAT_FACE_RIGHT;
    public static PImage CAT_BACK_ONLY;
    public static PImage CAT_OUTLINE;
    public static PGraphics BACK_OF_CARD;
    private final static String DEFAULT_CAT_FILE = "./data/Symmetrical Miaow.png";
    private final static String CAT_LEFT_FILE = "./data/Symmetrical Miaow Face Left.png";
    private final static String CAT_RIGHT_FILE = "./data/Symmetrical Miaow Face Right.png";
    private final static String CAT_BACK_FILE = "./data/Symmetrical Miaow Background.png";
    private final static String CAT_OUTLINE_FILE = "./data/Symmetrical Miaow Outline.png";
    private final static String BICYCLE_BACK_FILE = "./data/Bicycle Cat.png";
    private final static float WINDOW_WIDTH = 1366;
    private final static float WINDOW_HEIGHT = 708;
    private final int CHAT_GREY = color(150);
    private final int CHAT_INACTIVE_GREY = color(120);
    private final int CHAT_DARK_GREY = color(50);
    private static final int CAT_WIDTH = 150;
    private static final int CAT_HEIGHT = 150;
    private final ServerClientHeartsClient actualClient = this;
    private final static int MAX_CHAT_MSG_LEN = 128; // arbitrary
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
        size((int) WINDOW_WIDTH, (int) WINDOW_HEIGHT);
    }

    @Override
    // MODIFIES: this
    // EFFECTS: initializes variables
    public void setup() {
        initCats();
        clientState = new ClientState();
        frameRate(30);
        surface.setTitle("Server Hearts Client!");
    }

    // MODIFIES: this
    // EFFECTS: initializes the cat images
    private void initCats() {
        CAT_DEFAULT = loadImage(DEFAULT_CAT_FILE);
        CAT_FACE_LEFT = loadImage(CAT_LEFT_FILE);
        CAT_FACE_RIGHT = loadImage(CAT_RIGHT_FILE);
        CAT_BACK_ONLY = loadImage(CAT_BACK_FILE);
        CAT_OUTLINE = loadImage(CAT_OUTLINE_FILE);
        CAT_DEFAULT.resize(CAT_WIDTH, CAT_HEIGHT);
        CAT_FACE_LEFT.resize(CAT_WIDTH, CAT_HEIGHT);
        CAT_FACE_RIGHT.resize(CAT_WIDTH, CAT_HEIGHT);
        CAT_BACK_ONLY.resize(CAT_WIDTH, CAT_HEIGHT);
        CAT_OUTLINE.resize(CAT_WIDTH, CAT_HEIGHT);
        final int BICYCLE_HEIGHT = 294;
        final int BICYCLE_WIDTH = 192;
        PImage bicycleCat = loadImage(BICYCLE_BACK_FILE);
        bicycleCat.resize(192, 294);
        final float X = (240 - BICYCLE_WIDTH) / 2.0f;
        final float Y = (380 - BICYCLE_HEIGHT) / 2.0f;
        BACK_OF_CARD = createGraphics(240, 380);
        BACK_OF_CARD.beginDraw();
        BACK_OF_CARD.stroke(BLACK);
        BACK_OF_CARD.strokeWeight(2);
        BACK_OF_CARD.fill(LIGHT_RED);
        BACK_OF_CARD.rect(0, 0, 240, 380, 30);
        BACK_OF_CARD.image(bicycleCat, X, Y);
        BACK_OF_CARD.endDraw();
    }

    //</editor-fold>

    // EFFECTS: determines whether the client is active
    private boolean isClientInactive() {
        return client == null || !client.active();
    }

    //<editor-fold desc="Opening Menu">

    private final static float IP_ENTER_WIDTH = 225;
    private final static float IP_ENTER_HEIGHT = 30;
    private final static float IP_ENTER_BOX_X = 570.5f;
    private final static float IP_ENTER_BOX_Y = 100;

    private static String errorDisplayed = "";

    // MODIFIES: this
    // EFFECTS: attempts to load the client
    private void tryLoadClient() {
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
            if (client != null) {
                client.initialize();
                clientState.setPlayerNum(client.getPlayerNum());
            }
        } catch (ConnectionException e) {
            if (e.getMessage().equals(ERR_TIMED_OUT)) {
                errorDisplayed = CONNECTION_TIMEOUT;
            } else if (e.getMessage().equals(ERR_TOO_MANY_PLAYERS)) {
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
    // EFFECTS: feeds any messages from NewClient to clientState
    public void catchExtraMessages(ServerToClientMessage msg) {
        clientState.processNewMessage(this, msg);
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
        if (msg.equals(ERR_INVALID_MSG)) {
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

    @Override
    // MODIFIES: this
    // EFFECTS: code run when the mouse wheel is moved
    public void mouseWheel(MouseEvent event) {
        if (isChatActive) scrollChat(event.getCount());
    }

    //<editor-fold desc="Chat window">
    private boolean isChatActive = true;

    private final static float outerChatWindowWidth = 250;
    private final static float outerChatWindowX = WINDOW_WIDTH - outerChatWindowWidth - 20;
    private final static float outerChatWindowY = 0;
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
    private int YCoordinateOfGraphic = 0;

    private final static int MAX_LINES_CHAT_RECORDED = 200;

    private LinkedList<String> chatMessageRender = new LinkedList<>();
    private PGraphics textInChat;

    // MODIFIES: this
    // EFFECTS: adds additional strings to chat message list
    public void addNewMessages(String msg) {
        if (textWidth(msg) < outerChatWindowWidth - 20) {
            chatMessageRender.addFirst(msg);
        } else {
            LinkedList<String> incomingMessage = new LinkedList<>();
            int beginIndex = 0;
            int endIndex = 0;
            while (endIndex != msg.length()) {
                while (textWidth(msg.substring(beginIndex, endIndex)) < outerChatWindowWidth - 20) {
                    endIndex++;
                    if (endIndex == msg.length()) break;
                }
                if (textWidth(msg.substring(beginIndex, endIndex)) > outerChatWindowWidth - 20)
                    incomingMessage.addFirst(msg.substring(beginIndex, endIndex - 1));
                else incomingMessage.addFirst(msg.substring(beginIndex));
                beginIndex = endIndex - 1;
            }
            chatMessageRender.addAll(0, incomingMessage);
        }
        while (chatMessageRender.size() > MAX_LINES_CHAT_RECORDED) {
            chatMessageRender.removeLast();
        }
        recreatePGraphicsChat();
    }

    private int minHeight = Math.round(innerChatWindowY - outerChatWindowY - 20);
    private int heightOffset = 30;

    private boolean isAtBottom = false;

    // MODIFIES: this
    // EFFECTS: redraws the pgraphics for the internal chat window
    private void recreatePGraphicsChat() {
        final float textHeight = textAscent() - textDescent();
        textInChat = createGraphics(Math.round(outerChatWindowWidth), Math.max(minHeight, Math.round(textHeight * chatMessageRender.size()) + heightOffset));
        Iterator<String> iterator = chatMessageRender.descendingIterator();
        textInChat.beginDraw();
        textInChat.fill(BLACK);
        textInChat.textSize(15);
        textInChat.textAlign(LEFT, TOP);
        for (int i = 0; iterator.hasNext(); i++) {
            String lol = iterator.next();
            textInChat.text(lol, 10, (i * textHeight));
        }
        textInChat.endDraw();
        if (isAtBottom) scrollChatToBottom();
    }

    // MODIFIES: this
    // EFFECTS: scrolls the chat
    private void scrollChat(float amt) {
        YCoordinateOfGraphic += amt;
        if (YCoordinateOfGraphic < 0) {
            YCoordinateOfGraphic = 0;
            isAtBottom = false;
        } else {
            if (textInChat.height == minHeight) {
                YCoordinateOfGraphic = 0;
                isAtBottom = false;
            } else if (YCoordinateOfGraphic + minHeight + heightOffset > textInChat.height) {
                YCoordinateOfGraphic = Math.round(textInChat.height - minHeight - heightOffset);
                isAtBottom = true;
            } else {
                isAtBottom = false;
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: brings the chat window to the bottom
    private void scrollChatToBottom() {
        isAtBottom = true;
        if (textInChat == null) {
            YCoordinateOfGraphic = 0;
            return;
        }
        if (textInChat.height == minHeight) YCoordinateOfGraphic = 0;
        else {
            YCoordinateOfGraphic = Math.round(textInChat.height - minHeight - heightOffset);
        }
    }

    // MODIFIES: this
    // EFFECTS: updates whether the chat window is active
    private void updateChatWindowActivity() {
        if (isChatActive) {
            if ((mouseX < outerChatWindowX || mouseX > outerChatWindowX + outerChatWindowWidth) || (mouseY < outerChatWindowY || mouseY > outerChatWindowY + outerChatWindowHeight)) {
                isChatActive = false;
                scrollChatToBottom();
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
                scrollChatToBottom();
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
        if (textInChat != null)
            image(textInChat.get(0, YCoordinateOfGraphic, Math.round(outerChatWindowWidth), minHeight + heightOffset), outerChatWindowX, outerChatWindowY);
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

    private final static float[][] YOU_TEXT_POSITIONS = new float[][]{
            {375, 80},
            {675, 250},
            {375, 350},
            {105, 250}
    };

    @Override
    // MODIFIES: this
    // EFFECTS: runs FPS times a second to draw to a screen
    public void draw() {
        background(WHITE);
        image(BACK_OF_CARD, 0, 0);
        if (isClientInactive()) {
            drawIPEnterText();
            drawIPEnterBox();
        } else {
            drawChatWindow();
            if (client.available() > 0) {
                ServerToClientMessage scm = client.readServerToClientMessage();
                clientState.processNewMessage(this, scm);
                System.out.println("New Message from Server: " + scm);
            }
            fill(RED);
            textSize(20);
            textAlign(CENTER);
            final float[] coords = YOU_TEXT_POSITIONS[clientState.getPlayerNum() - 1];
            text("YOU", coords[0], coords[1]);
            image(clientState.getDrawnImages()[0], 300, 80);
            image(clientState.getDrawnImages()[3], 30, 250);
            image(clientState.getDrawnImages()[1], 600, 250);
            image(clientState.getDrawnImages()[2], 300, 350);
            if (clientState.isGameStarted()) {
                for (int i = 0; i < clientState.getDeck().deckSize(); i++) {
                    clientState.getDeck().get(i).draw(this, Card.CARD_WIDTH * i, height - Card.CARD_HEIGHT);
                }
            }
        }
    }
}
