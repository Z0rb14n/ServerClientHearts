package client.ui;

import client.ClientGameState;
import util.card.Card;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class DrawUtil {
    private static final Font font = new Font("Arial", Font.PLAIN, 20);

    public static void drawCard(Graphics2D g, int x, int y, int width, int height, Card c) {
        drawCard(g, x, y, width, height, c, ClientGameState.getInstance().isCardAllowed(c));
    }

    public static void drawCard(Graphics2D g, int x, int y, int width, int height, Card c, boolean allowed) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform af = g.getTransform();
        g.setStroke(new BasicStroke(2));
        g.translate(x, y);
        g.setColor(allowed ? Color.WHITE : Color.GRAY);
        g.fillRoundRect(0, 0, width, height, 20, 20);
        g.setColor(Color.BLACK);
        g.drawRoundRect(0, 0, width, height, 20, 20);
        g.setFont(font);
        g.setColor(c.getSuit().getColor());
        String firstLine = "" + c.getSuit().getCharacter();
        String secondLine = c.isFaceCard() ? "" + c.getFace() : "" + c.getNumber();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(firstLine, 15 - fm.stringWidth(firstLine) / 2, 15);
        g.drawString(secondLine, 15 - fm.stringWidth(secondLine) / 2, 35);
        g.scale(-1, -1);
        g.drawString(firstLine, -width + 15 - fm.stringWidth(firstLine) / 2, -height + 15);
        g.drawString(secondLine, -width + 15 - fm.stringWidth(secondLine) / 2, -height + 35);
        g.setTransform(af);
    }
}
