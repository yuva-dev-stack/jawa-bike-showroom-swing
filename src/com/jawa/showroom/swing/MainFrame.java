package com.jawa.showroom.swing;

import com.jawa.showroom.service.AuthService;
import com.jawa.showroom.service.BookingService;
import com.jawa.showroom.service.DataStore;

import javax.swing.*;
import java.awt.*;

/**
 * MainFrame — root application window.
 * Uses a CardLayout to switch between: LOGIN, REGISTER, DASHBOARD panels.
 * All child panels receive a reference to navigate back here.
 */
public class MainFrame extends JFrame {

    // ── Card Names ─────────────────────────────────────────────────────────────
    public static final String CARD_LOGIN     = "LOGIN";
    public static final String CARD_REGISTER  = "REGISTER";
    public static final String CARD_DASHBOARD = "DASHBOARD";

    // ── Services ───────────────────────────────────────────────────────────────
    private final DataStore      dataStore;
    private final AuthService    authService;
    private final BookingService bookingService;

    // ── Layout ─────────────────────────────────────────────────────────────────
    private final CardLayout   cardLayout = new CardLayout();
    private final JPanel       cardHolder = new JPanel(cardLayout);

    // ── Screens ────────────────────────────────────────────────────────────────
    private LoginPanel     loginPanel;
    private RegisterPanel  registerPanel;
    private DashboardFrame dashboardFrame;  // opened as separate window

    public MainFrame() {
        this.dataStore      = DataStore.getInstance();
        this.authService    = new AuthService(dataStore);
        this.bookingService = new BookingService(dataStore);

        buildUI();
    }

    private void buildUI() {
        setTitle("Jawa Bike Showroom — Online Booking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 640));
        setPreferredSize(new Dimension(520, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppTheme.BG_DARK);

        // Build individual screens
        loginPanel    = new LoginPanel(this, authService);
        registerPanel = new RegisterPanel(this, authService);

        // Add to card holder
        cardHolder.setBackground(AppTheme.BG_DARK);
        cardHolder.add(loginPanel,    CARD_LOGIN);
        cardHolder.add(registerPanel, CARD_REGISTER);

        add(cardHolder);

        // Start on login screen
        showCard(CARD_LOGIN);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    public void showCard(String name) {
        cardLayout.show(cardHolder, name);
    }

    /** Called after successful login — opens the full dashboard window */
    public void openDashboard() {
        setVisible(false);   // hide login window
        SwingUtilities.invokeLater(() -> {
            dashboardFrame = new DashboardFrame(this, authService, dataStore, bookingService);
            dashboardFrame.setVisible(true);
        });
    }

    /** Called when user logs out from dashboard */
    public void onLogout() {
        if (dashboardFrame != null) {
            dashboardFrame.dispose();
            dashboardFrame = null;
        }
        loginPanel.clearFields();
        setVisible(true);
        showCard(CARD_LOGIN);
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public AuthService    getAuthService()    { return authService; }
    public DataStore      getDataStore()      { return dataStore; }
    public BookingService getBookingService() { return bookingService; }
}
