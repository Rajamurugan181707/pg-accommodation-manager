package com.pgms.gui;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Rounded text box. The outline turns azure and thicker while you type in it. */
public class PgField extends JTextField {

    private static final long serialVersionUID = 1L;
    private final Icon icon;
    private String hint;

    public PgField(String text, int columns) {
        this(text, columns, null);
    }

    public PgField(String text, int columns, Icon icon) {
        super(text, columns);
        this.icon = icon;
        setOpaque(false);
        setForeground(Theme.INK);
        setCaretColor(Theme.INK);
        setFont(Theme.BODY);
        setBorder(BorderFactory.createEmptyBorder(8, icon == null ? 12 : 36, 8, 12));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    /** Words shown inside the box while it is empty, e.g. "Search by name". */
    public void setHint(String hint) {
        this.hint = hint;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Theme.BG2);
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        if (icon != null) {
            icon.paintIcon(this, g, 12, (getHeight() - icon.getIconHeight()) / 2);
        }
        if (hint != null && getText().isEmpty() && !hasFocus()) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Theme.INK);
            g.setFont(Theme.font(java.awt.Font.ITALIC, 14));
            java.awt.FontMetrics fm = g.getFontMetrics();
            g.drawString(hint, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
        }
        g.dispose();
        super.paintComponent(g0);
    }

    @Override
    protected void paintBorder(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean focus = hasFocus();
        g.setColor(focus ? Theme.AZURE : Theme.LINE2);
        g.setStroke(new BasicStroke(focus ? 2f : 1f));
        g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
        g.dispose();
    }
}
