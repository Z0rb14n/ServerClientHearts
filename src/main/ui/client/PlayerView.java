package ui.client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;

// Represents the view of the cats/players
public class PlayerView extends JPanel {
    private static BufferedImage catDefault;
    private static BufferedImage catFaceLeft;
    private static BufferedImage catFaceRight;
    private static BufferedImage catBackOnly;
    private static BufferedImage catOutlineOnly;
    private static BufferedImage bicycleCat;
    private final static Font font = new Font("Arial", Font.PLAIN, 20);
    private final static String DEFAULT_CAT_FILE = "./data/Symmetrical Miaow.png";
    private final static String CAT_LEFT_FILE = "./data/Symmetrical Miaow Face Left.png";
    private final static String CAT_RIGHT_FILE = "./data/Symmetrical Miaow Face Right.png";
    private final static String CAT_BACK_FILE = "./data/Symmetrical Miaow Background.png";
    private final static String CAT_OUTLINE_FILE = "./data/Symmetrical Miaow Outline.png";
    private final static String BICYCLE_BACK_FILE = "./data/Bicycle Cat.png";

    private static final int CAT_WIDTH = 150;
    private static final int CAT_HEIGHT = 150;

    // EFFECTS: initializes this PlayerView and initializes the cats
    PlayerView() {
        super();
        setPreferredSize(new Dimension(900, 200));
        initCats();
    }

    // EFFECTS: initializes all the cats
    public static void initCats() {
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

    // MODIFIES: this
    // EFFECTS: gets the outline cat image - if not initialized, initializes the cats
    //          throws new RuntimeException if cats cannot be initialized
    public static BufferedImage getOutlineCat() {
        if (catOutlineOnly == null) {
            initCats();
            if (catOutlineOnly == null) throw new RuntimeException();
        }
        return catOutlineOnly;
    }

    // MODIFIES: this
    // EFFECTS: gets the cat image - if not initialized, initializes the cats
    //          throws new RuntimeException if cats cannot be initialized
    public static BufferedImage getCatDefault() {
        if (catDefault == null) {
            initCats();
            if (catDefault == null) throw new RuntimeException();
        }
        return catDefault;
    }

    // MODIFIES: this
    // EFFECTS: gets the cat image facing left - if not initialized, initializes the cats
    //          throws new RuntimeException if cats cannot be initialized
    public static BufferedImage getCatFaceLeft() {
        if (catFaceLeft == null) {
            initCats();
            if (catFaceLeft == null) throw new RuntimeException();
        }
        return catFaceLeft;
    }

    // MODIFIES: this
    // EFFECTS: gets the cat image facing right - if not initialized, initializes the cats
    //          throws new RuntimeException if cats cannot be initialized
    public static BufferedImage getCatFaceRight() {
        if (catFaceRight == null) {
            initCats();
            if (catFaceRight == null) throw new RuntimeException();
        }
        return catFaceRight;
    }

    // MODIFIES: this
    // EFFECTS: gets the cat back image - if not initialized, initializes the cats
    //          throws new RuntimeException if cats cannot be initialized
    public static BufferedImage getCatBackOnly() {
        if (catBackOnly == null) {
            initCats();
            if (catBackOnly == null) throw new RuntimeException();
        }
        return catBackOnly;
    }

    // MODIFIES: this
    // EFFECTS: gets the cat riding the bicycle - if not initialized, initializes the cats
    //          throws new RuntimeException if cats cannot be initialized
    public static BufferedImage getBicycleCat() {
        if (bicycleCat == null) {
            initCats();
            if (bicycleCat == null) throw new RuntimeException();
        }
        return bicycleCat;
    }

    private final static int[][] CAT_COORDINATES = new int[][]{
            {300, 80},
            {600, 250},
            {300, 350},
            {30, 250}
    };

    private final static int[][] YOU_TEXT_POSITIONS = new int[][]{
            {375, 80},
            {675, 250},
            {375, 350},
            {105, 250}
    };

    @Override
    // MODIFIES: g
    // EFFECTS: paints this component onto the graphics object
    public void paintComponent(Graphics g) {
        final boolean[] existingPlayers = MainFrame.getFrame().getClientState().getExistingPlayers();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(font);
        g.setColor(Color.RED);
        FontMetrics fm = g.getFontMetrics();
        int x = YOU_TEXT_POSITIONS[MainFrame.getFrame().getClientState().getPlayerNum() - 1][0] - (fm.stringWidth("YOU") / 2);
        g.drawString("YOU", x, YOU_TEXT_POSITIONS[MainFrame.getFrame().getClientState().getPlayerNum() - 1][1]);
        if (existingPlayers[0]) g.drawImage(catDefault, CAT_COORDINATES[0][0], CAT_COORDINATES[0][1], null);
        else g.drawImage(catOutlineOnly, CAT_COORDINATES[0][0], CAT_COORDINATES[0][1], null);

        if (existingPlayers[1]) g.drawImage(catFaceLeft, CAT_COORDINATES[1][0], CAT_COORDINATES[1][1], null);
        else g.drawImage(catOutlineOnly, CAT_COORDINATES[1][0], CAT_COORDINATES[1][1], null);

        if (existingPlayers[2]) g.drawImage(catBackOnly, CAT_COORDINATES[2][0], CAT_COORDINATES[2][1], null);
        else g.drawImage(catOutlineOnly, CAT_COORDINATES[2][0], CAT_COORDINATES[2][1], null);

        if (existingPlayers[3]) g.drawImage(catFaceLeft, CAT_COORDINATES[3][0], CAT_COORDINATES[3][1], null);
        else g.drawImage(catOutlineOnly, CAT_COORDINATES[3][0], CAT_COORDINATES[3][1], null);
        g.drawImage(bicycleCat, 0, 0, null);
    }

    // EFFECTS: returns a resized image to the new width and new height
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
}
