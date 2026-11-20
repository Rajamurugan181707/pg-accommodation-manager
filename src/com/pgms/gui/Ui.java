package com.pgms.gui;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;

/** Small factory methods so every screen looks the same. */
public final class Ui {

    private Ui() {
    }

    /** Blue-to-violet gradient button with white text: the main action on a screen. */
    public static JButton primary(String text) {
        return button(text, null, "primary");
    }

    public static JButton primary(String text, String icon) {
        return button(text, icon, "primary");
    }

    /** Dark outlined button with white text. */
    public static JButton secondary(String text) {
        return button(text, null, "secondary");
    }

    public static JButton secondary(String text, String icon) {
        return button(text, icon, "secondary");
    }

    static JButton button(String text, String icon, String kind) {
        JButton b = new JButton(text);
        b.putClientProperty("pg.kind", kind);
        b.setForeground(Theme.INK);
        b.setFont(Theme.BOLD);
        if (icon != null) {
            b.setIcon(Icons.of(icon, 16, b.getForeground()));
        }
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.INK);
        l.setFont(Theme.BODY);
        return l;
    }

    public static JLabel bold(String text) {
        JLabel l = label(text);
        l.setFont(Theme.BOLD);
        return l;
    }

    public static JLabel small(String text) {
        JLabel l = label(text);
        l.setFont(Theme.SMALL);
        return l;
    }

    public static JLabel heading(String text) {
        JLabel l = label(text);
        l.setFont(Theme.H2);
        return l;
    }

    public static JLabel heading(String text, String icon) {
        JLabel l = heading(text);
        l.setIcon(Icons.of(icon, 18, Theme.INK));
        l.setIconTextGap(10);
        return l;
    }

    public static PgField field(int columns) {
        return new PgField("", columns);
    }

    public static PgField searchField(int columns) {
        return new PgField("", columns, Icons.of("search", 16, Theme.INK));
    }

    /** Dark rounded-looking drop-down. */
    public static <T> javax.swing.JComboBox<T> combo(T[] items) {
        javax.swing.JComboBox<T> c = new javax.swing.JComboBox<T>(items);
        c.setBackground(Theme.RAISE);
        c.setForeground(Theme.INK);
        c.setFont(Theme.BODY);
        c.setBorder(BorderFactory.createLineBorder(Theme.LINE2));
        c.setPreferredSize(new Dimension(Math.max(c.getPreferredSize().width + 10, 120), 38));
        return c;
    }

    /** A see-through checkbox with white text. */
    public static javax.swing.JCheckBox check(String text) {
        javax.swing.JCheckBox c = new javax.swing.JCheckBox(text);
        c.setOpaque(false);
        c.setForeground(Theme.INK);
        c.setFont(Theme.BODY);
        return c;
    }

    /** Say what just happened: in the status line and as a toast. */
    public static void done(JLabel status, String message) {
        status.setText(message);
        Toast.show(status, message);
    }

    /** A row of controls, left aligned. */
    public static JPanel row(Component... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        for (Component c : parts) {
            p.add(c);
        }
        return p;
    }

    /** A row of controls, right aligned. */
    public static JPanel rowRight(Component... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        for (Component c : parts) {
            p.add(c);
        }
        return p;
    }

    public static JTable table(DefaultTableModel model) {
        return new PgTable(model);
    }

    /** Set preferred column widths (pixels); the table still stretches to fill. */
    public static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            if (w[i] <= 60) {
                t.getColumnModel().getColumn(i).setMaxWidth(w[i] + 20);
            }
        }
    }

    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        s.getViewport().setBackground(Theme.BG2);
        s.setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(18);
        return s;
    }

    /** A see-through scroll pane for card grids. */
    public static JScrollPane clearScroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.setOpaque(false);
        s.getViewport().setOpaque(false);
        s.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        s.getVerticalScrollBar().setUnitIncrement(18);
        return s;
    }

    /** A rounded card: heading on top (optional), content below. */
    public static RoundPanel card(String title, JComponent content) {
        return card(title, null, content);
    }

    public static RoundPanel card(String title, String icon, JComponent content) {
        RoundPanel p = new RoundPanel(new BorderLayout(0, 12));
        if (title != null) {
            p.add(icon == null ? heading(title) : heading(title, icon), BorderLayout.NORTH);
        }
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    /** A small gap. */
    public static JComponent gap(int w) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(w, 1));
        return p;
    }

    public static void info(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Cannot do that", JOptionPane.PLAIN_MESSAGE);
    }

    public static boolean confirm(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Show a wrong-input or database problem in words. */
    public static void problem(Component parent, Exception e) {
        if (e instanceof SQLException) {
            error(parent, "Database error: " + e.getMessage());
        } else {
            error(parent, e.getMessage());
        }
    }

    /** A pop-up that shows a table, e.g. a statement or a bill. */
    public static void tableDialog(Component parent, String title, String top, DefaultTableModel model,
                                   String bottom) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Theme.PANEL);
        if (top != null) {
            p.add(heading(top), BorderLayout.NORTH);
        }
        JScrollPane s = scroll(table(model));
        s.setPreferredSize(new Dimension(720, Math.min(460, 44 + model.getRowCount() * 42)));
        p.add(s, BorderLayout.CENTER);
        if (bottom != null) {
            p.add(bold(bottom), BorderLayout.SOUTH);
        }
        JOptionPane.showMessageDialog(parent, p, title, JOptionPane.PLAIN_MESSAGE);
    }

    /** Icon helper for labels. */
    public static Icon icon(String name, int size) {
        return Icons.of(name, size, Theme.INK);
    }
}
