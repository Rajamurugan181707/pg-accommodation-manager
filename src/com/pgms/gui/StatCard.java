package com.pgms.gui;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** A dashboard number: title, big value, a caption line, and an optional progress bar. */
public class StatCard extends JComponent {

    private static final long serialVersionUID = 1L;
    private final String title;
    private final String icon;
    private final Color accent;
    private String value = "-";
    private String caption = " ";
    private double progress = -1;

    public StatCard(String title, String icon, Color accent) {
        this.title = title;
        this.icon = icon;
        this.accent = accent;
        setPreferredSize(new Dimension(220, 128));
    }

    public void set(String value, String caption, double progress) {
        this.value = value;
        this.caption = caption;
        this.progress = progress;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g.setPaint(new GradientPaint(0, 0, Theme.PANEL2, 0, h, Theme.PANEL));
        g.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);
        g.setColor(Theme.LINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
        // accent glow line on top
        g.setColor(accent);
        g.fillRoundRect(18, 0, 46, 4, 4, 4);

        // icon badge
        int d = 38;
        g.setColor(Theme.RAISE);
        g.fillOval(w - d - 16, 16, d, d);
        g.setColor(accent);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(w - d - 16, 16, d, d);
        Icons.of(icon, 18, Theme.INK).paintIcon(this, g, w - d - 16 + 10, 26);

        g.setColor(Theme.INK);
        g.setFont(Theme.SMALL);
        g.drawString(title.toUpperCase(), 18, 32);
        g.setFont(Theme.BIG);
        FontMetrics fm = g.getFontMetrics();
        String v = value;
        while (fm.stringWidth(v) > w - 36 && g.getFont().getSize() > 16) {
            g.setFont(Theme.font(java.awt.Font.BOLD, g.getFont().getSize() - 2));
            fm = g.getFontMetrics();
        }
        g.drawString(v, 18, 72);
        g.setFont(Theme.BODY);
        g.drawString(caption, 18, progress >= 0 ? 96 : 104);
        if (progress >= 0) {
            int bw = w - 36;
            g.setColor(Theme.LINE);
            g.fillRoundRect(18, 106, bw, 8, 8, 8);
            g.setColor(accent);
            g.fillRoundRect(18, 106, (int) Math.max(8, bw * Math.min(1, progress)), 8, 8, 8);
        }
        g.dispose();
    }
}
