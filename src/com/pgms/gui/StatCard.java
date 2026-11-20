package com.pgms.gui;

import javax.swing.JComponent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** A headline number on a bold gradient card: title, big value, caption, optional progress bar. */
public class StatCard extends JComponent {

    private static final long serialVersionUID = 1L;
    private final String title;
    private final String icon;
    private final Color from;
    private final Color to;
    private String value = "-";
    private String caption = " ";
    private double progress = -1;

    /** gradient = index into Theme.GRADS. */
    public StatCard(String title, String icon, int gradient) {
        this.title = title;
        this.icon = icon;
        this.from = Theme.GRADS[gradient % Theme.GRADS.length][0];
        this.to = Theme.GRADS[gradient % Theme.GRADS.length][1];
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
        g.setPaint(new GradientPaint(0, 0, from, w, h, to));
        g.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);
        // soft light circles for depth (graphics only)
        java.awt.Shape old = g.getClip();
        g.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w - 1, h - 1, 20, 20));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
        g.setColor(Color.WHITE);
        g.fillOval(w - 90, -40, 150, 150);
        g.fillOval(w - 40, h - 50, 90, 90);
        g.setComposite(AlphaComposite.SrcOver);
        g.setClip(old);

        // icon badge
        int d = 40;
        g.setColor(new Color(255, 255, 255, 50));
        g.fillOval(w - d - 16, 16, d, d);
        Icons.of(icon, 20, Theme.INK).paintIcon(this, g, w - d - 16 + 10, 26);

        g.setColor(Theme.INK);
        g.setFont(Theme.SMALL);
        g.drawString(title.toUpperCase(), 18, 32);
        g.setFont(Theme.BIG);
        FontMetrics fm = g.getFontMetrics();
        while (fm.stringWidth(value) > w - 36 && g.getFont().getSize() > 16) {
            g.setFont(Theme.font(Font.BOLD, g.getFont().getSize() - 2));
            fm = g.getFontMetrics();
        }
        g.drawString(value, 18, 72);
        g.setFont(Theme.BOLD);
        g.drawString(caption, 18, progress >= 0 ? 96 : 104);
        if (progress >= 0) {
            int bw = w - 36;
            g.setColor(new Color(255, 255, 255, 60));
            g.fillRoundRect(18, 106, bw, 8, 8, 8);
            g.setColor(Theme.INK);
            g.fillRoundRect(18, 106, (int) Math.max(8, bw * Math.min(1, progress)), 8, 8, 8);
        }
        g.dispose();
    }
}
