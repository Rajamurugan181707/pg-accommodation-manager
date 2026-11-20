package com.pgms.gui;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * A pop-up form: label + field rows, OK / Cancel.
 * If saving fails (wrong input), the message is shown and the form opens
 * again with everything still filled in.
 */
public class Form {

    /** What to do when the user presses OK. Throw to keep the form open. */
    public interface Submit {
        void run() throws Exception;
    }

    private final JPanel panel = new JPanel(new GridBagLayout());
    private int rows = 0;

    public Form() {
        panel.setBackground(Theme.PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public JTextField text(String label, String initial) {
        JTextField f = new PgField(initial == null ? "" : initial, 24);
        f.setName(label);
        add(label, f);
        return f;
    }

    public <T> JComboBox<T> combo(String label, T[] items) {
        JComboBox<T> c = Ui.combo(items);
        c.setName(label);
        add(label, c);
        return c;
    }

    public JCheckBox check(String label) {
        JCheckBox c = new JCheckBox();
        c.setName(label);
        c.setOpaque(false);
        add(label, c);
        return c;
    }

    /** A line of plain information above the fields. */
    public void note(String text) {
        JLabel l = Ui.label(text);
        l.setFont(Theme.BOLD);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = rows++;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(2, 2, 10, 2);
        panel.add(l, g);
    }

    private void add(String label, JComponent field) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = rows;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(6, 2, 6, 16);
        panel.add(Ui.bold(label), g);
        g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = rows++;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(6, 2, 6, 2);
        panel.add(field, g);
    }

    public JPanel panel() {
        return panel;
    }

    /** Returns true if saved, false if cancelled. */
    public boolean show(Component parent, String title, Submit onOk) {
        while (true) {
            int answer = JOptionPane.showConfirmDialog(parent, panel, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return false;
            }
            try {
                onOk.run();
                return true;
            } catch (Exception e) {
                Ui.problem(parent, e);
            }
        }
    }

    /** Read a number from a field, with the field's name in the message. */
    public static double amount(JTextField f) {
        return money(f, false);
    }

    /** Like amount, but 0 is allowed (e.g. no security deposit). */
    public static double amountOrZero(JTextField f) {
        return money(f, true);
    }

    private static double money(JTextField f, boolean allowZero) {
        String s = f.getText().trim().replace(",", "").replace("Rs.", "").trim();
        try {
            return com.pgms.util.Check.money(Double.parseDouble(s), allowZero, f.getName());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(f.getName() + ": enter an amount in rupees, e.g. 6500");
        }
    }

    public static int whole(JTextField f, int min, int max) {
        try {
            int n = Integer.parseInt(f.getText().trim());
            if (n >= min && n <= max) {
                return n;
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        throw new IllegalArgumentException(f.getName() + ": enter a whole number from " + min + " to " + max);
    }

    public static java.sql.Date date(JTextField f) {
        try {
            return com.pgms.util.Dates.parse(f.getText());
        } catch (java.text.ParseException e) {
            throw new IllegalArgumentException(f.getName() + ": use the form 2026-11-20");
        }
    }

    public static String required(JTextField f) {
        String s = f.getText().trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException(f.getName() + " cannot be empty");
        }
        return s;
    }

    public static String optional(JTextField f) {
        String s = f.getText().trim();
        return s.isEmpty() ? null : s;
    }
}
