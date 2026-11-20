package com.pgms.gui;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Paints every JButton as a rounded pill.
 * Kind (client property "pg.kind"): primary (bright azure, dark text), secondary
 * (dark, outlined, white text), nav / navOn (sidebar items). Hover and press
 * states change the shade.
 */
public class PgButtonUI extends BasicButtonUI {

    private static final PgButtonUI SHARED = new PgButtonUI();

    public static ComponentUI createUI(JComponent c) {
        return SHARED;
    }

    private static class PadBorder extends EmptyBorder implements UIResource {
        private static final long serialVersionUID = 1L;

        PadBorder(int t, int l, int b, int r) {
            super(t, l, b, r);
        }
    }

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        if (b.getBorder() == null || b.getBorder() instanceof UIResource) {
            b.setBorder(new PadBorder(9, 16, 9, 16));
        }
        b.setIconTextGap(8);
    }

    @Override
    public void paint(Graphics g0, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel m = b.getModel();
        String kind = String.valueOf(b.getClientProperty("pg.kind"));
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (!b.isEnabled()) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
        }
        int w = c.getWidth();
        int h = c.getHeight();
        int arc = 12;
        boolean hover = m.isRollover();
        boolean down = m.isArmed() && m.isPressed();

        if ("primary".equals(kind) || "navOn".equals(kind)) {
            Color fill = down ? Theme.AZURE_LO : hover && "primary".equals(kind) ? Theme.AZURE_HI : Theme.AZURE;
            g.setColor(fill);
            g.fillRoundRect(0, 0, w, h, arc, arc);
            if ("navOn".equals(kind)) {
                g.setColor(Theme.GOLD);
                g.fillRoundRect(0, 6, 4, h - 12, 4, 4);
            }
        } else if ("nav".equals(kind)) {
            if (hover || down) {
                g.setColor(down ? Theme.LINE2 : Theme.RAISE);
                g.fillRoundRect(0, 0, w, h, arc, arc);
            }
        } else {
            g.setColor(down ? Theme.LINE2 : hover ? Theme.HOVER : Theme.RAISE);
            g.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g.setColor(hover ? Theme.STEEL : Theme.LINE2);
            g.setStroke(new BasicStroke(1f));
            g.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        }
        super.paint(g, c);
        g.dispose();
    }

    @Override
    protected void paintText(Graphics g, AbstractButton b, java.awt.Rectangle textRect, String text) {
        // always draw text in the button's own colour, even when disabled (no grey text)
        java.awt.FontMetrics fm = g.getFontMetrics(b.getFont());
        g.setColor(b.getForeground());
        g.setFont(b.getFont());
        g.drawString(text, textRect.x, textRect.y + fm.getAscent());
    }
}
