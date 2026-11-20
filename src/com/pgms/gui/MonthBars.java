package com.pgms.gui;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DateFormatSymbols;

/**
 * Last six months. Each bar's OUTLINE is what was billed; the FILLED part
 * inside is what was collected, with the percentage written above it.
 */
public class MonthBars extends JComponent {

    private static final long serialVersionUID = 1L;
    private String[] months = new String[0];
    private double[] billed = new double[0];
    private double[] collected = new double[0];

    public MonthBars() {
        setPreferredSize(new Dimension(380, 190));
    }

    public void setData(String[] months, double[] billed, double[] collected) {
        this.months = months;
        this.billed = billed;
        this.collected = collected;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // legend
        g.setFont(Theme.SMALL);
        g.setColor(Theme.STEEL);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(0, 2, 14, 12, 4, 4);
        g.setColor(Theme.INK);
        g.drawString("BILLED (outline)", 22, 13);
        g.setPaint(new java.awt.GradientPaint(0, 2, Theme.MAGENTA, 0, 14, Theme.BLUE));
        g.fillRoundRect(150, 2, 14, 12, 4, 4);
        g.setColor(Theme.INK);
        g.drawString("COLLECTED (filled)", 172, 13);

        int top = 40;
        int base = h - 26;
        double max = 1;
        for (double b : billed) {
            max = Math.max(max, b);
        }
        g.setColor(Theme.LINE);
        for (int i = 0; i <= 3; i++) {
            int y = base - (base - top) * i / 3;
            g.drawLine(0, y, w, y);
        }
        int n = months.length;
        if (n == 0) {
            g.dispose();
            return;
        }
        int slot = w / n;
        int bw = Math.min(46, slot - 18);
        String[] names = new DateFormatSymbols().getShortMonths();
        for (int i = 0; i < n; i++) {
            int cx = slot * i + slot / 2;
            int bh = (int) ((base - top) * billed[i] / max);
            int ch = (int) ((base - top) * Math.min(collected[i], billed[i]) / max);
            if (billed[i] > 0) {
                g.setPaint(new java.awt.GradientPaint(0, base - ch, Theme.MAGENTA, 0, base, Theme.BLUE));
                g.fillRoundRect(cx - bw / 2, base - ch, bw, ch, 8, 8);
                g.setColor(Theme.STEEL);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(cx - bw / 2, base - bh, bw, bh, 8, 8);
                String pct = Math.round(100 * collected[i] / billed[i]) + "%";
                g.setColor(Theme.INK);
                g.setFont(Theme.SMALL);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(pct, cx - fm.stringWidth(pct) / 2, base - bh - 6);
            }
            String label = names[Integer.parseInt(months[i].substring(5)) - 1];
            g.setColor(Theme.INK);
            g.setFont(Theme.SMALL);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(label, cx - fm.stringWidth(label) / 2, h - 6);
        }
        g.dispose();
    }
}
