package com.pgms.gui;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Dark theme with pure white text everywhere (the user is colour blind).
 * Colour is used only in graphics (bars, rings, outlines, icons); every state is
 * also written in words. The one exception to white text: dark text on the
 * bright azure fills (primary buttons and the current page in the sidebar).
 */
public final class Theme {

    // Rich indigo base: dark enough for white text everywhere, colourful in tone
    public static final Color BG = new Color(0x0E0A2A);
    public static final Color BG2 = new Color(0x140F36);
    public static final Color PANEL = new Color(0x1B1544);
    public static final Color PANEL2 = new Color(0x241C57);
    public static final Color RAISE = new Color(0x2D2468);
    public static final Color HOVER = new Color(0x3A2F82);
    public static final Color LINE = new Color(0x332A70);
    public static final Color LINE2 = new Color(0x5244A6);
    public static final Color INK = Color.WHITE;
    public static final Color DARK_INK = new Color(0x120A2A);

    // Vivid accents (graphics and fills; text on them is white or, on the brightest, dark)
    public static final Color AZURE = new Color(0x4C8DFF);
    public static final Color AZURE_HI = new Color(0x7AAEFF);
    public static final Color AZURE_LO = new Color(0x3366E8);
    public static final Color BLUE = new Color(0x3D5BFF);
    public static final Color VIOLET = new Color(0x9B5CFF);
    public static final Color PURPLE = new Color(0x7A3CF0);
    public static final Color MAGENTA = new Color(0xFF4FB8);
    public static final Color PINK = new Color(0xD8338F);
    public static final Color AMBER = new Color(0xFFA62B);
    public static final Color ORANGE = new Color(0xF2711C);
    public static final Color CYAN = new Color(0x2EE6D6);
    public static final Color TEAL = new Color(0x0FA3B1);
    public static final Color GOLD = new Color(0xFFD84D);
    public static final Color STEEL = new Color(0x7B72B8);

    /** Gradient pairs for filled cards; all dark/saturated enough for white text. */
    public static final Color[][] GRADS = {
        {new Color(0x3D5BFF), new Color(0x8A3FFC)},   // blue -> violet
        {new Color(0x8A3FFC), new Color(0xD8338F)},   // violet -> magenta
        {new Color(0x0E8FA3), new Color(0x3D5BFF)},   // teal -> blue
        {new Color(0xE0601A), new Color(0xC2307A)},   // orange -> magenta
        {new Color(0xB8860B), new Color(0xE0601A)},   // gold -> orange
    };

    /** The primary-button gradient (white text). */
    public static final Color BTN_A = new Color(0x4361FF);
    public static final Color BTN_B = new Color(0x9B3FFC);

    /** First font family installed from a list that looks good on macOS and Windows. */
    public static final String FAMILY = pickFamily();

    public static final Font HERO = font(Font.BOLD, 30);
    public static final Font TITLE = font(Font.BOLD, 26);
    public static final Font H2 = font(Font.BOLD, 17);
    public static final Font BODY = font(Font.PLAIN, 14);
    public static final Font BOLD = font(Font.BOLD, 14);
    public static final Font SMALL = font(Font.BOLD, 12);
    public static final Font BIG = font(Font.BOLD, 28);

    private Theme() {
    }

    public static Font font(int style, int size) {
        return new Font(FAMILY, style, size);
    }

    private static String pickFamily() {
        try {
            Set<String> have = new HashSet<String>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String f : new String[] {"Avenir Next", "Segoe UI", "Helvetica Neue", "Arial"}) {
                if (have.contains(f)) {
                    return f;
                }
            }
        } catch (Throwable ignored) {
            // fall back below
        }
        return "SansSerif";
    }

    public static Border line() {
        return BorderFactory.createLineBorder(LINE2);
    }

    /** Switch Swing to Metal with our colours, then plug in our own button and scrollbar painters. */
    public static void install() {
        MetalLookAndFeel.setCurrentTheme(new DarkMetal());
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            // Metal ships with every JDK; if this ever fails the default look is still usable
        }
        UIManager.put("ButtonUI", PgButtonUI.class.getName());
        UIManager.put("ScrollBarUI", PgScrollBarUI.class.getName());
        UIManager.put("ScrollBar.width", 12);

        put("Panel.background", PANEL);
        put("OptionPane.background", PANEL);
        put("OptionPane.messageForeground", INK);
        put("Label.foreground", INK);
        put("Label.disabledForeground", INK);
        put("Viewport.background", PANEL);
        put("ScrollPane.background", PANEL);
        put("Table.background", BG2);
        put("Table.foreground", INK);
        put("Table.gridColor", LINE);
        put("Table.selectionBackground", LINE2);
        put("Table.selectionForeground", INK);
        put("TableHeader.background", PANEL2);
        put("TableHeader.foreground", INK);
        put("TextField.background", BG2);
        put("TextField.foreground", INK);
        put("TextField.caretForeground", INK);
        put("TextField.selectionBackground", LINE2);
        put("TextField.selectionForeground", INK);
        put("TextField.inactiveForeground", INK);
        put("TextField.inactiveBackground", BG2);
        put("ComboBox.background", RAISE);
        put("ComboBox.foreground", INK);
        put("ComboBox.selectionBackground", LINE2);
        put("ComboBox.selectionForeground", INK);
        put("ComboBox.disabledForeground", INK);
        put("ComboBox.buttonBackground", RAISE);
        put("List.background", BG2);
        put("List.foreground", INK);
        put("List.selectionBackground", LINE2);
        put("List.selectionForeground", INK);
        put("CheckBox.background", PANEL);
        put("CheckBox.foreground", INK);
        put("Button.background", RAISE);
        put("Button.foreground", INK);
        put("Button.disabledText", INK);
        put("ToolTip.background", RAISE);
        put("ToolTip.foreground", INK);
        UIManager.put("ComboBox.border", BorderFactory.createLineBorder(LINE2));
        UIManager.put("Table.font", new FontUIResource(BODY));
        UIManager.put("TableHeader.font", new FontUIResource(SMALL));
        UIManager.put("Label.font", new FontUIResource(BODY));
        UIManager.put("Button.font", new FontUIResource(BOLD));
        UIManager.put("TextField.font", new FontUIResource(BODY));
        UIManager.put("ComboBox.font", new FontUIResource(BODY));
        UIManager.put("CheckBox.font", new FontUIResource(BODY));
        UIManager.put("List.font", new FontUIResource(BODY));
        UIManager.put("ToolTip.font", new FontUIResource(BODY));
        UIManager.put("OptionPane.messageFont", new FontUIResource(BODY));
        UIManager.put("OptionPane.buttonFont", new FontUIResource(BOLD));
    }

    private static void put(String key, Color c) {
        UIManager.put(key, new ColorUIResource(c));
    }

    /** Metal theme: dark controls, white text, no light surfaces anywhere. */
    private static class DarkMetal extends DefaultMetalTheme {
        @Override public String getName() { return "PG Dark"; }
        @Override protected ColorUIResource getPrimary1() { return new ColorUIResource(AZURE); }
        @Override protected ColorUIResource getPrimary2() { return new ColorUIResource(LINE2); }
        @Override protected ColorUIResource getPrimary3() { return new ColorUIResource(RAISE); }
        @Override protected ColorUIResource getSecondary1() { return new ColorUIResource(LINE2); }
        @Override protected ColorUIResource getSecondary2() { return new ColorUIResource(LINE); }
        @Override protected ColorUIResource getSecondary3() { return new ColorUIResource(PANEL); }
        @Override protected ColorUIResource getBlack() { return new ColorUIResource(INK); }
        @Override protected ColorUIResource getWhite() { return new ColorUIResource(BG2); }
        @Override public ColorUIResource getControlTextColor() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getInactiveControlTextColor() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getInactiveSystemTextColor() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getSystemTextColor() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getUserTextColor() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getHighlightedTextColor() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getMenuForeground() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getWindowTitleForeground() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getControlHighlight() { return new ColorUIResource(LINE2); }
        @Override public ColorUIResource getControlDarkShadow() { return new ColorUIResource(LINE2); }
    }
}
