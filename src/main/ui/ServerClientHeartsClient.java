package ui;

// TODO FINISH GUI AFTER FINALIZING SERVER

import net.ConnectionException;
import net.NewClient;
import processing.core.PApplet;
import processing.core.PVector;
import processing.net.Client;

import java.util.Scanner;

// Represents Client + GUI
public class ServerClientHeartsClient extends PApplet {
    private final static int WHITE = 0xffffffff;
    private final static int BLACK = 0xff000000;
    private final static int RED = 0xffff0000;
    private final static float IPENTERWIDTH = 225;
    private final static float IPENTERHEIGHT = 30;
    private static final int port = ServerClientHearts.PORT;
    private static PVector topLeftIPEnter;
    private final ServerClientHeartsClient actualClient = this;
    private NewClient client;
    private boolean inGame = false;
    private boolean inStarting = true;
    private boolean failed = false;
    private boolean tooManyPlayers = false;
    private int ipEnterPosition = 0;
    private String ip = "";
    private int playerNum;

    // Main function to run
    public static void main(String[] args) {
        ServerClientHeartsClient sch = new ServerClientHeartsClient();
        PApplet.runSketch(new String[]{"lmao"}/*Processing arguments*/, sch);
    }

    @Override
    // MODIFIES: this
    // EFFECTS: sets size of window (see Processing for details)
    public void settings() {
        size(640, 480);
    }

    @Override
    // MODIFIES: this
    // EFFECTS: initializes variables
    public void setup() {
        topLeftIPEnter = new PVector(width / 2.0f - IPENTERWIDTH / 2.0f, 100);
        frameRate(30);
        surface.setTitle("Server Hearts Client!");
        TerminalMessageSender tms = new TerminalMessageSender();
        tms.start();
    }

    // MODIFIES: this
    // EFFECTS: attempts to load the client
    public void tryLoadClient() {
        try {
            Thread thread = new Thread(() -> client = new NewClient(actualClient, ip, port));
            thread.start();
            final long time = System.nanoTime();
            thread.join(10000);
            if (System.nanoTime() - time > (9000000) && client == null) {
                failed = true;
            }
        } catch (ConnectionException e) {
            System.out.print("HI");
            if (NewClient.TOO_MANY_PLAYERS.equals(e.getMessage())) {
                tooManyPlayers = true;
            }
            failed = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(2);
        }
        if (client == null) failed = true;
        if (!failed) {
            inStarting = false;
            playerNum = client.getPlayerNum();
        }
    }

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
        if (failed) {
            textAlign(CENTER, CENTER);
            textSize(12);
            fill(RED);
            if (!tooManyPlayers)
                text("Could not connect.", topLeftIPEnter.x + IPENTERWIDTH / 2, topLeftIPEnter.y + IPENTERHEIGHT + 20);
            else text("Too many players.", topLeftIPEnter.x + IPENTERWIDTH / 2, topLeftIPEnter.y + IPENTERHEIGHT + 20);
        }
    }

    @Override
    // MODIFIES: this
    // EFFECTS: code run when a key is pressed (see Processing)
    public void keyPressed() {
        if (inStarting) {
            if (key != CODED && key != BACKSPACE && key != RETURN && key != ENTER && key != TAB && key != ESC) {
                ip = ip.substring(0, ipEnterPosition) + key + ip.substring(ipEnterPosition);
                ipEnterPosition++;
                failed = false;
                tooManyPlayers = false;
            } else if (key == BACKSPACE) {
                if (ip.length() != 0 && ipEnterPosition != 0) {
                    ip = ip.substring(0, ipEnterPosition - 1) + ip.substring(ipEnterPosition);
                    ipEnterPosition--;
                    failed = false;
                    tooManyPlayers = false;
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
    }

    @Override
    // MODIFIES: this
    // EFFECTS: code run when the mouse is clicked
    public void mouseClicked() {
    }

    // EFFECTS: called when the client receives data from a server.
    public void clientEvent(Client c) {

    }

    // EFFECTS: called when a client disconnects from a server.
    public void disconnectEvent(Client c) {
        System.out.println("Client disconnected: " + c.ip());
    }

    @Override
    // MODIFIES: this
    // EFFECTS: runs FPS times a second to draw to a screen
    public void draw() {
        background(255);
        if (inStarting) {
            drawIPEnterText();
            drawIPEnterBox();
        } else {
            if (client.available() > 0) {
                System.out.println(client.readString());
            }
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
