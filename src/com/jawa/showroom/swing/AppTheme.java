package com.jawa.showroom.swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * AppTheme — centralised colour palette, fonts, and factory methods.
 * All Swing panels pull from here to ensure a consistent dark-red Jawa brand look.
 */
public final class AppTheme {

    // ── Colour Palette ─────────────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(18, 18, 24);      // near-black background
    public static final Color BG_CARD       = new Color(28, 28, 38);      // card / panel background
    public static final Color BG_SIDEBAR    = new Color(22, 22, 30);      // sidebar background
    public static final Color BG_INPUT      = new Color(35, 35, 48);      // input field background
    public static final Color BG_TABLE_ALT  = new Color(32, 32, 44);      // alternating table row

    public static final Color ACCENT_RED    = new Color(196, 30, 58);     // Jawa classic red
    public static final Color ACCENT_GOLD   = new Color(212, 175, 55);    // gold accent
    public static final Color ACCENT_HOVER  = new Color(220, 50, 80);     // button hover red

    public static final Color TEXT_PRIMARY  = new Color(240, 240, 245);   // main text
    public static final Color TEXT_SECONDARY= new Color(160, 160, 175);   // muted text
    public static final Color TEXT_ACCENT   = new Color(212, 175, 55);    // gold labels
    public static final Color TEXT_SUCCESS  = new Color(80, 200, 120);    // green success
    public static final Color TEXT_ERROR    = new Color(255, 80, 80);     // red error

    public static final Color BORDER_SUBTLE = new Color(50, 50, 65);      // subtle border
    public static final Color BORDER_ACCENT = new Color(196, 30, 58);     // red border

    // ── Fonts ──────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("SansSerif", Font.BOLD,  26);
    public static final Font FONT_HEADING  = new Font("SansSerif", Font.BOLD,  16);
    public static final Font FONT_SUBHEAD  = new Font("SansSerif", Font.BOLD,  13);
    public static final Font FONT_BODY     = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font FONT_BTN      = new Font("SansSerif", Font.BOLD,  13);
    public static final Font FONT_LABEL    = new Font("SansSerif", Font.BOLD,  12);
    public static final Font FONT_BRAND    = new Font("SansSerif", Font.BOLD,  32);

    private AppTheme() {}

    // ── Factory: Buttons ───────────────────────────────────────────────────────

    /** Primary red action button */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_RED);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(ACCENT_RED);   }
        });
        return btn;
    }

    /** Secondary outlined button */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setForeground(ACCENT_GOLD);
        btn.setBackground(BG_CARD);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_GOLD, 1, true),
                new EmptyBorder(9, 22, 9, 22)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(40,38,25)); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(BG_CARD);             }
        });
        return btn;
    }

    /** Ghost / flat text button for nav links */
    public static JButton linkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(null);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setForeground(TEXT_PRIMARY); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setForeground(TEXT_SECONDARY); }
        });
        return btn;
    }

    // ── Factory: Input Fields ──────────────────────────────────────────────────

    public static JTextField textField() {
        JTextField tf = new JTextField();
        styleInput(tf);
        return tf;
    }

    public static JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField();
        styleInput(pf);
        return pf;
    }

    public static JSpinner spinner(double min, double max, double step, double initial) {
        SpinnerNumberModel model = new SpinnerNumberModel(initial, min, max, step);
        JSpinner sp = new JSpinner(model);
        sp.setFont(FONT_BODY);
        sp.setBackground(BG_INPUT);
        sp.setForeground(TEXT_PRIMARY);
        JComponent editor = sp.getEditor();
        editor.setBackground(BG_INPUT);
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(BG_INPUT);
            de.getTextField().setForeground(TEXT_PRIMARY);
            de.getTextField().setFont(FONT_BODY);
            de.getTextField().setBorder(new EmptyBorder(4, 6, 4, 6));
            de.getTextField().setCaretColor(TEXT_PRIMARY);
        }
        return sp;
    }

    private static void styleInput(JTextField tf) {
        tf.setFont(FONT_BODY);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        tf.setSelectionColor(ACCENT_RED);
    }

    // ── Factory: Labels ────────────────────────────────────────────────────────

    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel headingLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel subLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    public static JLabel accentLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_ACCENT);
        return l;
    }

    // ── Factory: Panels ────────────────────────────────────────────────────────

    /** Standard dark card panel with rounded border */
    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1, true),
                new EmptyBorder(20, 24, 20, 24)));
        return p;
    }

    /** Dark background panel (no border) */
    public static JPanel darkPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_DARK);
        return p;
    }

    // ── Factory: Separators ────────────────────────────────────────────────────

    public static JSeparator redSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(ACCENT_RED);
        sep.setBackground(ACCENT_RED);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return sep;
    }

    // ── Table Styling ──────────────────────────────────────────────────────────

    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_BODY);
        table.setGridColor(BORDER_SUBTLE);
        table.setRowHeight(36);
        table.setSelectionBackground(ACCENT_RED);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(BG_DARK);
        table.getTableHeader().setForeground(TEXT_ACCENT);
        table.getTableHeader().setFont(FONT_SUBHEAD);
        table.getTableHeader().setBorder(new EmptyBorder(4, 10, 4, 10));
    }

    /** Sets the global Swing L&F to system default so native rendering works */
    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        // Override key UIDefaults for consistent dark theme
        UIManager.put("Panel.background",           BG_DARK);
        UIManager.put("OptionPane.background",       BG_CARD);
        UIManager.put("OptionPane.messageForeground",TEXT_PRIMARY);
        UIManager.put("Button.background",           ACCENT_RED);
        UIManager.put("Button.foreground",           Color.WHITE);
        UIManager.put("TextField.background",        BG_INPUT);
        UIManager.put("TextField.foreground",        TEXT_PRIMARY);
        UIManager.put("ScrollPane.background",       BG_DARK);
        UIManager.put("ScrollBar.background",        BG_DARK);
        UIManager.put("ScrollBar.thumb",             BORDER_SUBTLE);
        UIManager.put("ToolTip.background",          BG_CARD);
        UIManager.put("ToolTip.foreground",          TEXT_PRIMARY);
    }
}
