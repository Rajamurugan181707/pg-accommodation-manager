package com.pgms.gui;

import com.pgms.ds.DynamicArray;
import com.pgms.model.Room;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * One row per room, one bed shape per bed. A taken bed is FILLED, a free bed is
 * an EMPTY outline, and each row also says "full" or "2 free" in words.
 */
public class OccupancyChart extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int ROW = 40;
    private static final int TOP = 36;

    private DynamicArray<Room> rooms = new DynamicArray<Room>();

    public OccupancyChart() {
        setOpaque(false);
    }

    public void setRooms(DynamicArray<Room> rooms) {
        this.rooms = rooms;
        setPreferredSize(new Dimension(220, TOP + 8 + rooms.size() * ROW));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setFont(Theme.SMALL);
        bed(g, 2, 6, true);
        g.setColor(Theme.INK);
        g.drawString("TAKEN", 34, 18);
        bed(g, 96, 6, false);
        g.setColor(Theme.INK);
        g.drawString("FREE", 128, 18);

        int y = TOP;
        int w = getWidth();
        for (Room r : rooms) {
            g.setColor(Theme.LINE);
            g.drawLine(0, y + ROW - 4, w, y + ROW - 4);
            g.setFont(Theme.BOLD);
            g.setColor(Theme.INK);
            g.drawString(r.getRoomNo(), 2, y + 20);
            int x = 56;
            for (int b = 0; b < r.getCapacity(); b++) {
                bed(g, x, y + 4, b < r.getOccupied());
                x += 34;
            }
            String word = r.isFull() ? "full" : r.vacantBeds() + " free";
            g.setFont(Theme.SMALL);
            FontMetrics fm = g.getFontMetrics();
            int pw = fm.stringWidth(word) + 20;
            int px = w - pw - 4;
            g.setColor(Theme.PANEL2);
            g.fillRoundRect(px, y + 4, pw, 22, 22, 22);
            g.setColor(r.isFull() ? Theme.STEEL : Theme.CYAN);
            g.setStroke(new BasicStroke(1.3f));
            g.drawRoundRect(px, y + 4, pw, 22, 22, 22);
            g.setColor(Theme.INK);
            g.drawString(word, px + 10, y + 20);
            y += ROW;
        }
        g.dispose();
    }

    /** A little bed: frame plus pillow. */
    static void bed(Graphics2D g, int x, int y, boolean taken) {
        int w = 26;
        int h = 18;
        g.setStroke(new BasicStroke(1.6f));
        if (taken) {
            g.setColor(Theme.AZURE);
            g.fillRoundRect(x, y, w, h, 6, 6);
            g.setColor(Theme.DARK_INK);
            g.fillRoundRect(x + 3, y + 3, 8, 6, 3, 3);
        } else {
            g.setColor(Theme.STEEL);
            g.drawRoundRect(x, y, w, h, 6, 6);
            g.drawRoundRect(x + 3, y + 3, 8, 6, 3, 3);
        }
    }
}
