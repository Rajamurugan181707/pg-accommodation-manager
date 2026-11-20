package com.pgms.gui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Read-only table with tall rows, hover highlight, money aligned right,
 * initials avatars in the name column and status words drawn as pills.
 * Each pill also has its own marker shape, so the state never depends on colour:
 *   PAID / ACTIVE = tick,  PARTIAL = half-filled circle,  UNPAID = empty circle,  LEFT = dash.
 */
public class PgTable extends JTable {

    private static final long serialVersionUID = 1L;
    static final Set<String> PILLS = new HashSet<String>(Arrays.asList("PAID", "PARTIAL", "UNPAID", "ACTIVE", "LEFT"));
    static final Set<String> NAME_COLUMNS = new HashSet<String>(Arrays.asList("Name", "Tenant"));
    static final Set<String> RIGHT_COLUMNS = new HashSet<String>(Arrays.asList(
            "Bill", "Paid", "Pending", "Amount", "Per month", "Deposit", "Rent per bed"));

    private int hoverRow = -1;

    public PgTable(DefaultTableModel model) {
        super(model);
        setRowHeight(42);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBackground(Theme.BG2);
        setForeground(Theme.INK);
        setFont(Theme.BODY);
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setResizingAllowed(true);
        getTableHeader().setPreferredSize(new Dimension(10, 38));
        getTableHeader().setDefaultRenderer(new Header());
        setDefaultRenderer(Object.class, new Cell());
        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int r = rowAtPoint(e.getPoint());
                if (r != hoverRow) {
                    hoverRow = r;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                repaint();
            }
        };
        addMouseMotionListener(hover);
        addMouseListener(hover);
    }

    private class Header extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);
            String name = String.valueOf(v);
            l.setText(name.toUpperCase());
            l.setFont(Theme.SMALL);
            l.setForeground(Theme.INK);
            l.setBackground(Theme.PANEL2);
            l.setOpaque(true);
            l.setHorizontalAlignment(RIGHT_COLUMNS.contains(name) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.LINE2),
                    BorderFactory.createEmptyBorder(0, 14, 0, 14)));
            return l;
        }
    }

    private class Cell extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private boolean pill;
        private boolean avatar;
        private Color rowColor;
        private String text = "";

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean selected, boolean f, int row, int col) {
            super.getTableCellRendererComponent(t, v, selected, false, row, col);
            text = v == null ? "" : String.valueOf(v);
            String column = t.getColumnName(col);
            pill = PILLS.contains(text);
            avatar = NAME_COLUMNS.contains(column) && !text.isEmpty();
            rowColor = selected ? Theme.LINE2 : row == hoverRow ? Theme.HOVER : (row % 2 == 0 ? Theme.BG2 : Theme.PANEL);
            setOpaque(false);
            setForeground(Theme.INK);
            setFont(selected || avatar ? Theme.BOLD : Theme.BODY);
            boolean right = RIGHT_COLUMNS.contains(column) || text.startsWith("Rs.");
            setHorizontalAlignment(right ? SwingConstants.RIGHT : SwingConstants.LEFT);
            int left = pill ? 40 : avatar ? 52 : 14;
            setBorder(BorderFactory.createEmptyBorder(0, left, 0, 14));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g.setColor(rowColor);
            g.fillRect(0, 0, w, h);
            g.setColor(Theme.LINE);
            g.fillRect(0, h - 1, w, 1);
            if (avatar) {
                paintAvatar(g, h);
            }
            if (pill) {
                paintPill(g, h);
            }
            g.dispose();
            super.paintComponent(g0);
        }

        private void paintAvatar(Graphics2D g, int h) {
            int d = 28;
            int x = 14;
            int y = (h - d) / 2;
            g.setColor(Theme.RAISE);
            g.fillOval(x, y, d, d);
            g.setColor(Theme.STEEL);
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(x, y, d, d);
            String initials = initials(text);
            g.setFont(Theme.SMALL);
            g.setColor(Theme.INK);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(initials, x + (d - fm.stringWidth(initials)) / 2, y + (d + fm.getAscent() - fm.getDescent()) / 2);
        }

        private void paintPill(Graphics2D g, int h) {
            FontMetrics fm = g.getFontMetrics(getFont());
            int pw = fm.stringWidth(text) + 40;
            int ph = 26;
            int x = 12;
            int y = (h - ph) / 2;
            Color edge = "PAID".equals(text) || "ACTIVE".equals(text) ? Theme.CYAN
                    : "LEFT".equals(text) ? Theme.STEEL : Theme.AMBER;
            g.setColor(Theme.PANEL2);
            g.fillRoundRect(x, y, pw, ph, ph, ph);
            g.setColor(edge);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(x, y, pw, ph, ph, ph);
            // marker shape
            int mx = x + 10;
            int my = y + 8;
            int m = 10;
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if ("PAID".equals(text) || "ACTIVE".equals(text)) {
                Path2D p = new Path2D.Double();
                p.moveTo(mx, my + 5);
                p.lineTo(mx + 4, my + 9);
                p.lineTo(mx + 11, my + 1);
                g.draw(p);
            } else if ("PARTIAL".equals(text)) {
                g.draw(new Ellipse2D.Double(mx, my, m, m));
                g.fillArc(mx, my, m, m, 90, 180);
            } else if ("UNPAID".equals(text)) {
                g.draw(new Ellipse2D.Double(mx, my, m, m));
            } else {
                g.drawLine(mx, my + 5, mx + m, my + 5);
            }
        }
    }

    static String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty() && Character.isLetter(p.charAt(0)) && sb.length() < 2) {
                sb.append(Character.toUpperCase(p.charAt(0)));
            }
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }
}
