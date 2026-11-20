package com.pgms.gui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/** A card: rounded corners, a soft top-to-bottom shade and a thin outline. */
public class RoundPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final int arc;
    private Color accent;

    public RoundPanel(LayoutManager layout) {
        this(layout, 18);
    }

    public RoundPanel(LayoutManager layout, int arc) {
        super(layout);
        this.arc = arc;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
    }

    /** A coloured strip down the left edge (graphics only, never the only signal). */
    public void setAccent(Color accent) {
        this.accent = accent;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g.setPaint(new GradientPaint(0, 0, Theme.PANEL2, 0, h, Theme.PANEL));
        g.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
        if (accent != null) {
            g.setClip(0, 0, 5, h);
            g.setColor(accent);
            g.fillRoundRect(0, 0, 12, h - 1, arc, arc);
            g.setClip(null);
        }
        g.setColor(Theme.LINE);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        g.dispose();
        super.paintComponent(g0);
    }
}
