package com.pgms.gui;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/** Slim rounded scrollbars without arrow buttons. */
public class PgScrollBarUI extends BasicScrollBarUI {

    public static ComponentUI createUI(JComponent c) {
        return new PgScrollBarUI();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return zero();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return zero();
    }

    private static JButton zero() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        g.setColor(Theme.BG2);
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    @Override
    protected void paintThumb(Graphics g0, JComponent c, Rectangle r) {
        if (r.isEmpty()) {
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(isThumbRollover() ? Theme.STEEL : Theme.LINE2);
        g.fillRoundRect(r.x + 3, r.y + 3, r.width - 6, r.height - 6, 8, 8);
        g.dispose();
    }
}
