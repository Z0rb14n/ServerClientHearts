package ui;

// TODO FINISH GUI AFTER FINALIZING SERVER

import net.ConnectionException;
import net.NewClient;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import util.ClientState;

import java.awt.*;
import java.util.Scanner;

// Represents Client + GUI
public final class ServerClientHeartsClient extends PApplet {
    private final static int WHITE = 0xffffffff;
    private final static int BLACK = 0xff000000;
    private final static int RED = 0xffff0000;
    private final static float IPENTERWIDTH = 225;
    private final static float IPENTERHEIGHT = 30;
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
    private final int CHAT_DARK_GREY = color(50);
    private static final int CAT_WIDTH = 150;
    private static final int CAT_HEIGHT = 150;
    private static PVector topLeftIPEnter;
    private final ServerClientHeartsClient actualClient = this;
    private final static int MAX_CHAT_MSG_LEN = 128;
    private ClientState clientState;
    private NewClient client;

    // Main function to run
    public static void main(String[] args) {
        ServerClientHeartsClient sch = new ServerClientHeartsClient();
        PApplet.runSketch(new String[]{"lmao"}/*Processing arguments*/, sch);
    }

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
        topLeftIPEnter = new PVector(width / 2.0f - IPENTERWIDTH / 2.0f, 100);
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

    // EFFECTS: determines whether the client is active
    private boolean isClientInactive() {
        return client == null || !client.active();
    }

    //<editor-fold desc="Opening Menu">

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
        text("Enter IP", topLeftIPEnter.x + IPENTERWIDTH / 2, topLeftIPEnter.y - IPENTERHEIGHT);
    }

    // MODIFIES: this
    // EFFECTS: drops the IP enter box
    private void drawIPEnterBox() {
        fill(WHITE);
        stroke(BLACK);
        strokeWeight(3);
        rect(topLeftIPEnter.x, topLeftIPEnter.y, IPENTERWIDTH, IPENTERHEIGHT);
        fill(BLACK);
        textAlign(LEFT, TOP);
        textSize(24);
        String displayedIP = ip;
        while (textWidth(displayedIP) > IPENTERWIDTH) {
            displayedIP = displayedIP.substring(0, displayedIP.length() - 1);
        }
        text(displayedIP, topLeftIPEnter.x, topLeftIPEnter.y);
        if (textWidth(ip.substring(0, ipEnterPosition)) <= IPENTERWIDTH && frameCount % 30 < 15) {
            fill(BLACK);
            noStroke();
            float xpos = topLeftIPEnter.x;
            if (ipEnterPosition != 0) {
                xpos += textWidth(ip.substring(0, ipEnterPosition));
            } else {
                xpos += 2;
            }
            rect(xpos, topLeftIPEnter.y + 2, 2, IPENTERHEIGHT - 4);
        }
        if (!"".equals(errorDisplayed)) {
            textAlign(CENTER, CENTER);
            textSize(12);
            fill(RED);
            text(errorDisplayed, topLeftIPEnter.x + IPENTERWIDTH / 2, topLeftIPEnter.y + IPENTERHEIGHT + 20);
        }
    }

    // MODIFIES: this
    // EFFECTS: add/removes/returns characters for the IP Enter Box
    private void modifyIPEnterBox() {
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

    //</editor-fold>

    @Override
    // MODIFIES: this
    // EFFECTS: code run when a key is pressed (see Processing)
    public void keyPressed() {
        if (isClientInactive()) {
            modifyIPEnterBox();
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

    // MODIFIES: this
    // EFFECTS: draws the chat window
    private void drawChatWindow() {
        fill(CHAT_GREY);
        rect(50, height - 200, 200, 150);
        fill(CHAT_DARK_GREY);
        rect(60, height - 90, 180, 25);
    }

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
                System.out.println(clientMessage);

            }
            if (clientState.getPlayerNum() == 1) {
                fill(RED);
                textAlign(CENTER);
                text("YOU", 375, 80);
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
