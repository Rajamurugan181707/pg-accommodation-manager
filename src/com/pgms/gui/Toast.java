package com.pgms.gui;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** A short confirmation that slides up in the bottom-right corner and fades away. */
public class Toast extends JComponent {

    private static final long serialVersionUID = 1L;
    private static Toast current;
    private final String text;
    private float alpha = 0f;

    private Toast(String text) {
        this.text = text;
        setFont(Theme.BOLD);
    }

    public static void show(Component anywhere, String text) {
        JRootPane root = SwingUtilities.getRootPane(anywhere);
        if (root == null || text == null) {
            return;
        }
        final JLayeredPane layer = root.getLayeredPane();
        if (current != null && current.getParent() != null) {
            current.getParent().remove(current);
            layer.repaint();
        }
        final Toast t = new Toast(text);
        current = t;
        FontMetrics fm = t.getFontMetrics(t.getFont());
        int w = Math.min(layer.getWidth() - 48, fm.stringWidth(text) + 76);
        int h = 54;
        final int x = layer.getWidth() - w - 28;
        final int yEnd = layer.getHeight() - h - 28;
        t.setBounds(x, yEnd + 20, w, h);
        layer.add(t, JLayeredPane.POPUP_LAYER);
        final long start = System.currentTimeMillis();
        final Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            long ms = System.currentTimeMillis() - start;
            if (ms < 220) {
                t.alpha = ms / 220f;
                t.setLocation(x, yEnd + (int) (20 * (1 - t.alpha)));
            } else if (ms < 3000) {
                t.alpha = 1f;
                t.setLocation(x, yEnd);
            } else if (ms < 3400) {
                t.alpha = 1f - (ms - 3000) / 400f;
            } else {
                timer.stop();
                if (t.getParent() != null) {
                    layer.remove(t);
                }
                layer.repaint();
                return;
            }
            t.repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
        int w = getWidth();
        int h = getHeight();
        g.setColor(Theme.RAISE);
        g.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
        g.setColor(Theme.CYAN);
        g.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
        g.setColor(Theme.CYAN);
        g.fillOval(16, (h - 26) / 2, 26, 26);
        Icons.of("check", 16, Theme.DARK_INK).paintIcon(this, g, 21, (h - 16) / 2);
        g.setColor(Theme.INK);
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, 54, (h + fm.getAscent() - fm.getDescent()) / 2);
        g.dispose();
    }
}
