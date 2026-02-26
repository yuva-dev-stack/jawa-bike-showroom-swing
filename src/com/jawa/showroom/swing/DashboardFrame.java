package com.jawa.showroom.swing;

import com.jawa.showroom.model.User;
import com.jawa.showroom.service.AuthService;
import com.jawa.showroom.service.BookingService;
import com.jawa.showroom.service.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * DashboardFrame — the full-featured main window shown after login.
 *
 * Layout:
 *  ┌─────────────┬────────────────────────────────┐
 *  │  SIDEBAR    │   CONTENT AREA (CardLayout)    │
 *  │  • Bikes    │                                │
 *  │  • EMI Calc │                                │
 *  │  • Bookings │                                │
 *  │  • Profile  │                                │
 *  │  • Logout   │                                │
 *  └─────────────┴────────────────────────────────┘
 */
public class DashboardFrame extends JFrame {

    // Card names
    public static final String CARD_BIKES    = "BIKES";
    public static final String CARD_EMI      = "EMI";
    public static final String CARD_BOOKINGS = "BOOKINGS";
    public static final String CARD_PROFILE  = "PROFILE";

    private final MainFrame    mainFrame;
    private final AuthService  authService;
    private final DataStore    dataStore;
    private final BookingService bookingService;

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel     contentArea   = new JPanel(contentLayout);

    // Nav buttons kept for highlight toggling
    private JButton btnBikes, btnEmi, btnBookings, btnProfile;
    private JButton activeBtn = null;

    // Panels
    private BikeListPanel    bikeListPanel;
    private EMIPanel         emiPanel;
    private BookingsPanel    bookingsPanel;
    private ProfilePanel     profilePanel;

    public DashboardFrame(MainFrame mainFrame, AuthService authService,
                          DataStore dataStore, BookingService bookingService) {
        this.mainFrame      = mainFrame;
        this.authService    = authService;
        this.dataStore      = dataStore;
        this.bookingService = bookingService;
        buildUI();
    }

    private void buildUI() {
        setTitle("Jawa Bike Showroom — Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { doLogout(); }
        });
        setMinimumSize(new Dimension(1100, 700));
        setPreferredSize(new Dimension(1200, 750));
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppTheme.BG_DARK);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildSidebar(), buildContent());
        split.setDividerLocation(220);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(AppTheme.BG_DARK);

        add(split);
        pack();

        // Default view
        navigate(CARD_BIKES, btnBikes);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppTheme.BG_SIDEBAR);
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));
        sidebar.setPreferredSize(new Dimension(220, 700));

        // Brand strip at top
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(AppTheme.ACCENT_RED);
        brand.setBorder(new EmptyBorder(24, 20, 24, 20));
        brand.setMaximumSize(new Dimension(220, 100));

        JLabel logo = new JLabel("JAWA");
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Bike Showroom");
        tagline.setFont(AppTheme.FONT_SMALL);
        tagline.setForeground(new Color(255, 200, 200));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(logo);
        brand.add(tagline);
        sidebar.add(brand);

        // User greeting
        User user = authService.getCurrentUser();
        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));
        userBox.setBackground(AppTheme.BG_SIDEBAR);
        userBox.setBorder(new EmptyBorder(16, 20, 16, 20));
        userBox.setMaximumSize(new Dimension(220, 70));

        JLabel greeting = new JLabel("Welcome back,");
        greeting.setFont(AppTheme.FONT_SMALL);
        greeting.setForeground(AppTheme.TEXT_SECONDARY);
        JLabel uname = new JLabel(user.getFullName().split(" ")[0] + " ✦");
        uname.setFont(AppTheme.FONT_SUBHEAD);
        uname.setForeground(AppTheme.ACCENT_GOLD);
        userBox.add(greeting);
        userBox.add(uname);
        sidebar.add(userBox);

        // Divider
        sidebar.add(makeSidebarDivider());

        // Nav items
        btnBikes    = navButton("🏍  Browse Bikes",   CARD_BIKES);
        btnEmi      = navButton("📊  EMI Calculator", CARD_EMI);
        btnBookings = navButton("📋  My Bookings",    CARD_BOOKINGS);
        btnProfile  = navButton("👤  My Profile",     CARD_PROFILE);

        sidebar.add(btnBikes);
        sidebar.add(btnEmi);
        sidebar.add(btnBookings);
        sidebar.add(btnProfile);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(makeSidebarDivider());

        // Logout button
        JButton logoutBtn = new JButton("⏻  Logout");
        logoutBtn.setFont(AppTheme.FONT_BTN);
        logoutBtn.setForeground(AppTheme.TEXT_ERROR);
        logoutBtn.setBackground(AppTheme.BG_SIDEBAR);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setBorder(new EmptyBorder(14, 20, 14, 20));
        logoutBtn.setMaximumSize(new Dimension(220, 48));
        logoutBtn.addActionListener(e -> doLogout());
        logoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { logoutBtn.setBackground(new Color(40,20,20)); }
            public void mouseExited (java.awt.event.MouseEvent e) { logoutBtn.setBackground(AppTheme.BG_SIDEBAR); }
        });
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(20));

        return sidebar;
    }

    private JButton navButton(String text, String card) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_BTN);
        btn.setForeground(AppTheme.TEXT_SECONDARY);
        btn.setBackground(AppTheme.BG_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(14, 20, 14, 20));
        btn.setMaximumSize(new Dimension(220, 48));
        btn.addActionListener(e -> navigate(card, btn));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(new Color(32, 32, 45));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(AppTheme.BG_SIDEBAR);
            }
        });
        return btn;
    }

    private JSeparator makeSidebarDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.BORDER_SUBTLE);
        sep.setBackground(AppTheme.BORDER_SUBTLE);
        sep.setMaximumSize(new Dimension(220, 1));
        return sep;
    }

    // ── Content Area ──────────────────────────────────────────────────────────

    private JPanel buildContent() {
        User user = authService.getCurrentUser();

        bikeListPanel = new BikeListPanel(dataStore, bookingService, user, this);
        emiPanel      = new EMIPanel();
        bookingsPanel = new BookingsPanel(dataStore, bookingService, user);
        profilePanel  = new ProfilePanel(user);

        contentArea.setBackground(AppTheme.BG_DARK);
        contentArea.add(bikeListPanel, CARD_BIKES);
        contentArea.add(emiPanel,      CARD_EMI);
        contentArea.add(bookingsPanel, CARD_BOOKINGS);
        contentArea.add(profilePanel,  CARD_PROFILE);
        return contentArea;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public void navigate(String card, JButton btn) {
        contentLayout.show(contentArea, card);

        // Highlight active nav button
        if (activeBtn != null) {
            activeBtn.setForeground(AppTheme.TEXT_SECONDARY);
            activeBtn.setBackground(AppTheme.BG_SIDEBAR);
        }
        if (btn != null) {
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(196, 30, 58, 40));
            activeBtn = btn;
        }

        // Refresh data-heavy panels when navigated to
        if (CARD_BOOKINGS.equals(card)) bookingsPanel.refresh();
    }

    public void navigateToBookings() {
        navigate(CARD_BOOKINGS, btnBookings);
    }

    private void doLogout() {
        authService.logout();
        mainFrame.onLogout();
    }
}
