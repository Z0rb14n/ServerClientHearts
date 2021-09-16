package server.ui;

import server.GameServer;

import javax.swing.*;
import java.awt.*;

public class ServerFrame extends JFrame {
    private static ServerFrame ourInstance;
    private static final Dimension SIZE = new Dimension(640, 480);
    private final Timer updateTimer = new Timer(100, e -> repaint());
    public final GameServer gameServer = new GameServer();

    public static ServerFrame getInstance() {
        if (ourInstance == null) {
            ourInstance = new ServerFrame();
        }
        return ourInstance;
    }

    private ServerFrame() {
        super("Server Client Hearts Server");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(SIZE);
        setBackground(Color.WHITE);
        setSize(SIZE);
        updateTimer.start();
        setVisible(true);
    }

    @Override
    // MODIFIES: g, this
    // EFFECTS: paints the given graphics object and updates the frame
    public void paint(Graphics g) {
        super.paint(g);
        gameServer.update();
    }

    @Override
    // MODIFIES: this
    // EFFECTS: disposes the jframe
    public void dispose() {
        updateTimer.stop();
        gameServer.dispose();
        super.dispose();
    }
}
