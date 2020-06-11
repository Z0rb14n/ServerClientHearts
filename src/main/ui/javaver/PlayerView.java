package ui.javaver;

import util.ClientState;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;

class PlayerView extends JPanel {
    public static BufferedImage catDefault;
    public static BufferedImage catFaceLeft;
    public static BufferedImage catFaceRight;
    public static BufferedImage catBackOnly;
    public static BufferedImage catOutlineOnly;
    public static BufferedImage bicycleCat;
    private final static String DEFAULT_CAT_FILE = "./data/Symmetrical Miaow.png";
    private final static String CAT_LEFT_FILE = "./data/Symmetrical Miaow Face Left.png";
    private final static String CAT_RIGHT_FILE = "./data/Symmetrical Miaow Face Right.png";
    private final static String CAT_BACK_FILE = "./data/Symmetrical Miaow Background.png";
    private final static String CAT_OUTLINE_FILE = "./data/Symmetrical Miaow Outline.png";
    private final static String BICYCLE_BACK_FILE = "./data/Bicycle Cat.png";

    private static final int CAT_WIDTH = 150;
    private static final int CAT_HEIGHT = 150;
    PlayerView() {
        super();
        setPreferredSize(new Dimension(900, 200));
        try {
            catDefault = resize(ImageIO.read(new File(DEFAULT_CAT_FILE)), CAT_WIDTH, CAT_HEIGHT);
            catFaceLeft = resize(ImageIO.read(new File(CAT_LEFT_FILE)), CAT_WIDTH, CAT_HEIGHT);
            catFaceRight = resize(ImageIO.read(new File(CAT_RIGHT_FILE)), CAT_WIDTH, CAT_HEIGHT);
            catBackOnly = resize(ImageIO.read(new File(CAT_BACK_FILE)), CAT_WIDTH, CAT_HEIGHT);
            catOutlineOnly = resize(ImageIO.read(new File(CAT_OUTLINE_FILE)), CAT_WIDTH, CAT_HEIGHT);
            bicycleCat = new BufferedImage(240, 380, BufferedImage.TYPE_INT_ARGB);
            final int BICYCLE_HEIGHT = 294;
            final int BICYCLE_WIDTH = 192;
            Graphics2D g = (Graphics2D) bicycleCat.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final int x = Math.floorDiv(240 - BICYCLE_WIDTH, 2);
            final int y = Math.floorDiv(380 - BICYCLE_HEIGHT, 2);
            Color LIGHT_RED = new Color(255, 0x66, 0x66);
            g.setStroke(new BasicStroke(2));
            g.setColor(LIGHT_RED);
            g.fillRoundRect(0, 0, 240, 380, 30, 30);
            g.setColor(Color.BLACK);
            g.drawRoundRect(0, 0, 240, 380, 30, 30);
            g.drawImage(ImageIO.read(new File(BICYCLE_BACK_FILE)), x, y, BICYCLE_WIDTH, BICYCLE_HEIGHT, LIGHT_RED, null);
            g.dispose();

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(catDefault, 0, 0, null);
        g.drawImage(catFaceLeft, 150, 0, null);
        g.drawImage(catFaceRight, 300, 0, null);
        g.drawImage(catBackOnly, 450, 0, null);
        g.drawImage(catOutlineOnly, 600, 0, null);
        g.drawImage(bicycleCat, 0, 150, null);
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        final int w = img.getWidth();
        final int h = img.getHeight();
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale((double) newW / w, (double) newH / h);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(img, after);
        return after;
    }

    void update(ClientState state) {

    }
}
