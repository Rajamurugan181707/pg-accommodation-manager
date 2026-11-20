package com.pgms.gui;

import com.pgms.util.Money;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;

/**
 * Ring showing how much of this month's bill is collected.
 * Collected = solid azure arc; pending = amber arc drawn thinner, so the two
 * parts differ in thickness as well as colour. Numbers are written beside it.
 */
public class DonutChart extends JComponent {

    private static final long serialVersionUID = 1L;
    private double collected;
    private double pending;

    public DonutChart() {
        setPreferredSize(new Dimension(380, 190));
    }

    public void setData(double collected, double pending) {
        this.collected = collected;
        this.pending = pending;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double total = collected + pending;
        double share = total == 0 ? 0 : collected / total;
        int d = Math.min(getHeight() - 24, 140);
        int x = 10;
        int y = (getHeight() - d) / 2;

        g.setStroke(new BasicStroke(10f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.setColor(Theme.LINE);
        g.draw(new Arc2D.Double(x, y, d, d, 0, 360, Arc2D.OPEN));
        if (pending > 0) {
            g.setColor(Theme.AMBER);
            g.draw(new Arc2D.Double(x, y, d, d, 90 - 360 * share, -360 * (1 - share), Arc2D.OPEN));
        }
        if (collected > 0) {
            g.setStroke(new BasicStroke(18f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g.setColor(Theme.AZURE);
            g.draw(new Arc2D.Double(x, y, d, d, 90, -360 * share, Arc2D.OPEN));
        }

        g.setColor(Theme.INK);
        String pct = Math.round(share * 100) + "%";
        g.setFont(Theme.BIG);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(pct, x + (d - fm.stringWidth(pct)) / 2, y + d / 2 + 6);
        g.setFont(Theme.SMALL);
        fm = g.getFontMetrics();
        String sub = "COLLECTED";
        g.drawString(sub, x + (d - fm.stringWidth(sub)) / 2, y + d / 2 + 26);

        // legend with numbers
        int lx = x + d + 22;
        int ly = y + 22;
        legend(g, lx, ly, true, "Collected", Money.inr(collected));
        legend(g, lx, ly + 48, false, "Pending", Money.inr(pending));
        g.setColor(Theme.LINE);
        g.fillRect(lx, ly + 84, 110, 1);
        g.setColor(Theme.INK);
        g.setFont(Theme.SMALL);
        g.drawString("BILLED", lx, ly + 104);
        g.setFont(Theme.BOLD);
        g.drawString(Money.inr(total), lx, ly + 122);
        g.dispose();
    }

    private void legend(Graphics2D g, int x, int y, boolean thick, String name, String amount) {
        g.setColor(thick ? Theme.AZURE : Theme.AMBER);
        g.fillRoundRect(x, thick ? y - 10 : y - 7, 18, thick ? 10 : 4, 3, 3);
        g.setColor(Theme.INK);
        g.setFont(Theme.SMALL);
        g.drawString(name.toUpperCase(), x + 26, y);
        g.setFont(Theme.BOLD);
        g.drawString(amount, x, y + 20);
    }
}
