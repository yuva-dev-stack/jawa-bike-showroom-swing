package com.jawa.showroom.swing;

import com.jawa.showroom.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * LoginPanel — full-screen login page with Jawa branding,
 * username / password fields, login button, and a register link.
 */
public class LoginPanel extends JPanel {

    private final MainFrame    mainFrame;
    private final AuthService  authService;

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private int            attempts = 0;

    public LoginPanel(MainFrame mainFrame, AuthService authService) {
        this.mainFrame   = mainFrame;
        this.authService = authService;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_DARK);

        // ── TOP BRAND HEADER ──────────────────────────────────────────────────
        JPanel header = buildBrandHeader();
        add(header, BorderLayout.NORTH);

        // ── CENTRE FORM CARD ──────────────────────────────────────────────────
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(AppTheme.BG_DARK);
        centre.setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel card = buildFormCard();
        centre.add(card, new GridBagConstraints());
        add(centre, BorderLayout.CENTER);

        // ── BOTTOM FOOTER ─────────────────────────────────────────────────────
        JPanel footer = buildFooter();
        add(footer, BorderLayout.SOUTH);
    }

    // ── Brand Header ──────────────────────────────────────────────────────────

    private JPanel buildBrandHeader() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(AppTheme.BG_DARK);
        p.setBorder(new EmptyBorder(40, 20, 20, 20));

        // Jawa logo canvas
        JPanel logoCanvas = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Red circular badge
                g2.setColor(AppTheme.ACCENT_RED);
                g2.fillOval(getWidth()/2-36, 4, 72, 72);

                // J letter
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 42));
                FontMetrics fm = g2.getFontMetrics();
                String letter = "J";
                int lx = getWidth()/2 - fm.stringWidth(letter)/2;
                g2.drawString(letter, lx, 57);
            }
        };
        logoCanvas.setPreferredSize(new Dimension(80, 80));
        logoCanvas.setOpaque(false);
        logoCanvas.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brand = new JLabel("JAWA");
        brand.setFont(AppTheme.FONT_BRAND);
        brand.setForeground(AppTheme.TEXT_PRIMARY);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = AppTheme.subLabel("BIKE SHOWROOM — ONLINE BOOKING");
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.ACCENT_RED);
        sep.setMaximumSize(new Dimension(200, 2));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(logoCanvas);
        p.add(Box.createVerticalStrut(10));
        p.add(brand);
        p.add(Box.createVerticalStrut(4));
        p.add(tagline);
        p.add(Box.createVerticalStrut(16));
        p.add(sep);
        return p;
    }

    // ── Form Card ─────────────────────────────────────────────────────────────

    private JPanel buildFormCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(30, 36, 30, 36)));

        JLabel heading = AppTheme.headingLabel("Sign In to Your Account");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = AppTheme.subLabel("Enter your credentials to continue");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label (hidden initially)
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.TEXT_ERROR);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username row
        JLabel userLbl = AppTheme.accentLabel("USERNAME");
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField = AppTheme.textField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(340, 40));
        usernameField.setPreferredSize(new Dimension(340, 40));

        // Password row
        JLabel pwdLbl = AppTheme.accentLabel("PASSWORD");
        pwdLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = AppTheme.passwordField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(340, 40));
        passwordField.setPreferredSize(new Dimension(340, 40));

        // Login button
        JButton loginBtn = AppTheme.primaryButton("SIGN IN");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(340, 44));
        loginBtn.addActionListener(this::onLogin);

        // Enter key triggers login
        passwordField.addActionListener(this::onLogin);
        usernameField.addActionListener(this::onLogin);

        // Register link
        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        registerRow.setBackground(AppTheme.BG_CARD);
        registerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noAcc = AppTheme.subLabel("Don't have an account? ");
        JButton regBtn = AppTheme.linkButton("Register here →");
        regBtn.setForeground(AppTheme.ACCENT_GOLD);
        regBtn.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_REGISTER));
        registerRow.add(noAcc);
        registerRow.add(regBtn);

        // Assemble
        card.add(heading);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(20));
        card.add(AppTheme.redSeparator());
        card.add(Box.createVerticalStrut(20));
        card.add(userLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));
        card.add(pwdLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(16));
        card.add(registerRow);

        return card;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(AppTheme.BG_DARK);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel l = AppTheme.subLabel("© 2025 Jawa Motorcycles — Est. 1929 | Pune, Maharashtra");
        l.setFont(AppTheme.FONT_SMALL);
        p.add(l);
        return p;
    }

    // ── Action Handlers ────────────────────────────────────────────────────────

    private void onLogin(ActionEvent e) {
        if (attempts >= 3) {
            errorLabel.setText("Too many failed attempts. Restart the application.");
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        String error = authService.login(username, password);

        if (error == null) {
            attempts = 0;
            errorLabel.setText(" ");
            mainFrame.openDashboard();
        } else {
            attempts++;
            int remaining = 3 - attempts;
            errorLabel.setText(error + (remaining > 0 ? " (" + remaining + " attempts left)" : ""));
            passwordField.setText("");
        }
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
        attempts = 0;
    }
}
