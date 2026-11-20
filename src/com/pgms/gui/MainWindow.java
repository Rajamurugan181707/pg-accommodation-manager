package com.pgms.gui;

import com.pgms.db.DBConnection;
import com.pgms.service.RentService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Swing window for the PG Accommodation Management System.
 * Left: sidebar with the logo and one button per page; the current page is the
 * bright button with a gold bar. Right: the page title and the page itself.
 */
public class MainWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    static final String[] PAGES = {"Dashboard", "Rooms", "Tenants", "Rent", "Mess"};
    private static final String[] ICONS = {"home", "bed", "users", "receipt", "bowl"};
    private static final String[] SUBTITLES = {
        "Today at a glance: beds, tenants and this month's money",
        "Every room, its beds and its rent. Click a room card to select it.",
        "Who is staying, who has left, and moving people around",
        "Monthly bills, payments and who still has to pay",
        "Meal plans, headcount and this week's menu"
    };

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Screen[] screens = {
        new DashboardPanel(), new RoomsPanel(), new TenantsPanel(), new RentPanel(), new MessPanel()
    };
    private final JButton[] nav = new JButton[PAGES.length];
    private final JLabel pageTitle = Ui.label("");
    private final JLabel pageSub = Ui.label("");

    public MainWindow() {
        super("PG Accommodation Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());
        add(sidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(Theme.BG);
        main.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        pageTitle.setFont(Theme.HERO);
        titles.add(pageTitle, BorderLayout.NORTH);
        titles.add(new RainbowLine(), BorderLayout.CENTER);
        titles.add(pageSub, BorderLayout.SOUTH);
        header.add(titles, BorderLayout.WEST);
        RoundPanel date = new RoundPanel(new BorderLayout(), 14);
        date.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 16));
        JLabel today = Ui.bold(new SimpleDateFormat("EEE, d MMM yyyy").format(new Date()));
        today.setIcon(Ui.icon("calendar", 16));
        today.setIconTextGap(10);
        date.add(today);
        JPanel dateWrap = new JPanel(new BorderLayout());
        dateWrap.setOpaque(false);
        dateWrap.add(date, BorderLayout.NORTH);
        header.add(dateWrap, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        content.setOpaque(false);
        for (int i = 0; i < PAGES.length; i++) {
            content.add((JComponent) screens[i], PAGES[i]);
        }
        main.add(content, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        setMinimumSize(new Dimension(1180, 760));
        setSize(1320, 840);
        setLocationRelativeTo(null);
        open(0);
    }

    private JComponent sidebar() {
        JPanel side = new JPanel(new BorderLayout(0, 20)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0;
                g.setPaint(new GradientPaint(0, 0, new java.awt.Color(0x1B1250), 0, getHeight(),
                        new java.awt.Color(0x3A1670)));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Theme.LINE2);
                g.fillRect(getWidth() - 1, 0, 1, getHeight());
            }
        };
        side.setPreferredSize(new Dimension(236, 10));
        side.setBorder(BorderFactory.createEmptyBorder(22, 16, 20, 16));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(new LogoMark(), BorderLayout.WEST);
        JPanel names = new JPanel(new GridLayout(2, 1));
        names.setOpaque(false);
        JLabel app = Ui.heading("PG Manager");
        app.setFont(Theme.font(java.awt.Font.BOLD, 19));
        names.add(app);
        names.add(Ui.small("ACCOMMODATION"));
        brand.add(names, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, 26));
        top.setOpaque(false);
        top.add(brand, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(PAGES.length, 1, 0, 6));
        buttons.setOpaque(false);
        for (int i = 0; i < PAGES.length; i++) {
            final int page = i;
            nav[i] = Ui.button(PAGES[i], null, "nav");
            nav[i].setHorizontalAlignment(SwingConstants.LEFT);
            nav[i].setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 14));
            nav[i].setIconTextGap(14);
            nav[i].setFont(Theme.font(java.awt.Font.BOLD, 15));
            nav[i].addActionListener(e -> open(page));
            buttons.add(nav[i]);
        }
        top.add(buttons, BorderLayout.CENTER);
        side.add(top, BorderLayout.NORTH);

        RoundPanel footer = new RoundPanel(new GridLayout(4, 1, 0, 2), 16);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel db = Ui.small("DATABASE");
        db.setIcon(Ui.icon("db", 14));
        db.setIconTextGap(8);
        footer.add(db);
        footer.add(Ui.bold("MySQL connected"));
        footer.add(Ui.bold("A. Raja Murugan"));
        footer.add(Ui.small("II CSE F  |  MINI PROJECT"));
        side.add(footer, BorderLayout.SOUTH);
        return side;
    }

    /** Show a page and reload its data. */
    public void open(int page) {
        for (int i = 0; i < nav.length; i++) {
            boolean on = i == page;
            nav[i].putClientProperty("pg.kind", on ? "navOn" : "nav");
            nav[i].setForeground(on ? Theme.DARK_INK : Theme.INK);
            nav[i].setIcon(Icons.of(ICONS[i], 18, on ? Theme.DARK_INK : Theme.INK));
            nav[i].repaint();
        }
        pageTitle.setText(PAGES[page]);
        pageSub.setText(SUBTITLES[page]);
        cards.show(content, PAGES[page]);
        screens[page].refresh();
    }

    Screen screen(int page) {
        return screens[page];
    }

    /** A short blue-magenta-amber bar under the page title (decoration only). */
    private static class RainbowLine extends JComponent {
        private static final long serialVersionUID = 1L;

        RainbowLine() {
            setPreferredSize(new Dimension(120, 10));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new java.awt.LinearGradientPaint(0, 0, 120, 0, new float[] {0f, 0.5f, 1f},
                    new java.awt.Color[] {Theme.BLUE, Theme.MAGENTA, Theme.AMBER}));
            g.fillRoundRect(0, 2, 120, 5, 5, 5);
            g.dispose();
        }
    }

    /** The "PG" logo: a magenta-to-blue rounded square with white letters and a gold bar. */
    private static class LogoMark extends JComponent {
        private static final long serialVersionUID = 1L;

        LogoMark() {
            setPreferredSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, Theme.MAGENTA, 44, 44, Theme.BLUE));
            g.fillRoundRect(0, 0, 44, 44, 14, 14);
            g.setColor(Theme.GOLD);
            g.fillRoundRect(8, 34, 28, 4, 4, 4);
            g.setColor(Theme.INK);
            g.setFont(Theme.font(java.awt.Font.BOLD, 18));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("PG", (44 - fm.stringWidth("PG")) / 2, 27);
            g.dispose();
        }
    }

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> {
            try {
                DBConnection.testConnection();
                new RentService().billEveryoneUpToNow();
            } catch (Exception e) {
                Ui.error(null, "Cannot connect to MySQL: " + e.getMessage()
                        + "\n\nCheck that MySQL is running, sql/schema.sql has been loaded,"
                        + "\nand the user / password in db.properties are right.");
                System.exit(1);
            }
            new MainWindow().setVisible(true);
        });
    }
}
