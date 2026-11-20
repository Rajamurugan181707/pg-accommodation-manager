package com.pgms.gui;

import com.pgms.model.Room;
import com.pgms.util.Money;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** One room as a card: number, type, a bed shape per bed (filled = taken), rent and free beds in words. */
public class RoomCard extends JComponent {

    private static final long serialVersionUID = 1L;
    private final Room room;
    private boolean selected;
    private boolean hover;

    public RoomCard(Room room, final Runnable onClick) {
        this.room = room;
        setPreferredSize(new Dimension(224, 190));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Room " + room.getRoomNo() + ": click to select");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                onClick.run();
            }
        });
    }

    public Room room() {
        return room;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g.setPaint(new GradientPaint(0, 0, hover || selected ? Theme.HOVER : Theme.PANEL2, 0, h, Theme.PANEL));
        g.fillRoundRect(1, 1, w - 3, h - 3, 18, 18);
        // colour band across the top: one gradient per room type
        java.awt.Color[] band = Theme.GRADS[room.getType().ordinal() == 0 ? 0 : room.getType().ordinal() == 1 ? 1 : 2];
        java.awt.Shape old = g.getClip();
        g.clip(new java.awt.geom.RoundRectangle2D.Float(1, 1, w - 3, h - 3, 18, 18));
        g.setPaint(new GradientPaint(0, 0, band[0], w, 0, band[1]));
        g.fillRect(0, 0, w, 56);
        g.setClip(old);
        if (selected) {
            g.setColor(Theme.MAGENTA);
            g.setStroke(new BasicStroke(3f));
        } else {
            g.setColor(hover ? Theme.STEEL : Theme.LINE);
            g.setStroke(new BasicStroke(1f));
        }
        g.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);

        // room number
        g.setColor(Theme.INK);
        g.setFont(Theme.TITLE);
        g.drawString(room.getRoomNo(), 18, 42);

        // top-right tag: SELECTED (words) or the floor
        String tag = selected ? "SELECTED" : room.getFloor() == 0 ? "GROUND" : "FLOOR " + room.getFloor();
        g.setFont(Theme.SMALL);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(tag) + (selected ? 34 : 20);
        int tx = w - tw - 14;
        g.setColor(new java.awt.Color(0, 0, 0, 70));
        g.fillRoundRect(tx, 18, tw, 24, 24, 24);
        g.setColor(selected ? Theme.MAGENTA : new java.awt.Color(255, 255, 255, 140));
        g.setStroke(new BasicStroke(selected ? 2f : 1.4f));
        g.drawRoundRect(tx, 18, tw, 24, 24, 24);
        if (selected) {
            Icons.of("check", 14, Theme.INK).paintIcon(this, g, tx + 9, 23);
        }
        g.setColor(Theme.INK);
        g.drawString(tag, tx + (selected ? 26 : 10), 35);

        // type line
        String type = room.getType().name().charAt(0) + room.getType().name().substring(1).toLowerCase();
        g.setFont(Theme.BODY);
        g.drawString(type + " room  |  " + (room.hasAc() ? "AC" : "Non-AC")
                + (selected ? "  |  " + (room.getFloor() == 0 ? "Ground" : "Floor " + room.getFloor()) : ""), 18, 78);

        // beds
        int x = 18;
        for (int b = 0; b < room.getCapacity(); b++) {
            drawBed(g, x, 90, b < room.getOccupied());
            x += 46;
        }

        // rent + free pill
        g.setColor(Theme.LINE);
        g.fillRect(16, h - 54, w - 32, 1);
        g.setColor(Theme.INK);
        g.setFont(Theme.H2);
        String rent = Money.inr(room.getRentPerBed());
        g.drawString(rent, 18, h - 22);
        int rw = g.getFontMetrics().stringWidth(rent);
        g.setFont(Theme.SMALL);
        g.drawString("/ BED", 18 + rw + 6, h - 22);

        String word = room.isFull() ? "FULL" : room.vacantBeds() + " FREE";
        fm = g.getFontMetrics();
        int pw = fm.stringWidth(word) + 22;
        int px = w - pw - 16;
        g.setColor(room.isFull() ? new java.awt.Color(0x3A3270) : new java.awt.Color(0x0B5E68));
        g.fillRoundRect(px, h - 40, pw, 26, 26, 26);
        g.setColor(room.isFull() ? Theme.STEEL : Theme.CYAN);
        g.drawRoundRect(px, h - 40, pw, 26, 26, 26);
        g.setColor(Theme.INK);
        g.drawString(word, px + 11, h - 22);
        g.dispose();
    }

    /** Bigger bed: frame, pillow and blanket line. Filled when taken, outlined when free. */
    private static void drawBed(Graphics2D g, int x, int y, boolean taken) {
        int w = 38;
        int h = 26;
        g.setStroke(new BasicStroke(1.8f));
        if (taken) {
            g.setPaint(new GradientPaint(x, y, Theme.GOLD, x + w, y + h, Theme.AMBER));
            g.fillRoundRect(x, y, w, h, 8, 8);
            g.setColor(Theme.DARK_INK);
            g.fillRoundRect(x + 4, y + 4, 11, 8, 4, 4);
            g.drawLine(x + 19, y + 4, x + 19, y + h - 4);
        } else {
            g.setColor(Theme.STEEL);
            g.drawRoundRect(x, y, w, h, 8, 8);
            g.drawRoundRect(x + 4, y + 4, 11, 8, 4, 4);
        }
    }
}
