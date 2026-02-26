package com.jawa.showroom.swing;

import com.jawa.showroom.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * RegisterPanel — new user registration form.
 * Validates all fields client-side before calling AuthService.
 */
public class RegisterPanel extends JPanel {

    private final MainFrame   mainFrame;
    private final AuthService authService;

    // Form fields
    private JTextField     usernameField, fullNameField, emailField, phoneField;
    private JTextField     streetField, cityField, stateField, pincodeField;
    private JPasswordField passwordField, confirmField;
    private JLabel         errorLabel, successLabel;

    public RegisterPanel(MainFrame mainFrame, AuthService authService) {
        this.mainFrame   = mainFrame;
        this.authService = authService;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_DARK);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BG_DARK);
        header.setBorder(new EmptyBorder(30, 40, 10, 40));

        JLabel title = AppTheme.titleLabel("Create Account");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = AppTheme.subLabel("Join the Jawa family — it only takes a minute.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        add(header, BorderLayout.NORTH);

        // ── Scrollable Form ───────────────────────────────────────────────────
        JPanel form = buildForm();
        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.setBackground(AppTheme.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(AppTheme.BG_DARK);
        outer.setBorder(new EmptyBorder(10, 40, 30, 40));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(28, 32, 28, 32)));
        card.setMaximumSize(new Dimension(460, Integer.MAX_VALUE));

        // ── Account fields ────────────────────────────────────────────────────
        addSection(card, "ACCOUNT DETAILS");

        usernameField = addField(card, "USERNAME", "Min 4 chars, letters/digits/_");
        passwordField = addPasswordField(card, "PASSWORD", "Min 8 chars, letters + digits");
        confirmField  = addPasswordField(card, "CONFIRM PASSWORD", "Re-enter your password");

        // ── Personal fields ───────────────────────────────────────────────────
        addSection(card, "PERSONAL INFORMATION");

        fullNameField = addField(card, "FULL NAME", "Your legal name");
        emailField    = addField(card, "EMAIL ADDRESS", "you@example.com");
        phoneField    = addField(card, "MOBILE NUMBER", "10-digit Indian number");

        // ── Address fields ────────────────────────────────────────────────────
        addSection(card, "ADDRESS");

        streetField  = addField(card, "STREET / FLAT NO.", "Building, street name");
        cityField    = addField(card, "CITY", "Your city");
        stateField   = addField(card, "STATE", "Your state");
        pincodeField = addField(card, "PIN CODE", "6-digit PIN");

        // ── Status labels ─────────────────────────────────────────────────────
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.TEXT_ERROR);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        successLabel = new JLabel(" ");
        successLabel.setFont(AppTheme.FONT_SMALL);
        successLabel.setForeground(AppTheme.TEXT_SUCCESS);
        successLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Buttons ───────────────────────────────────────────────────────────
        JButton registerBtn = AppTheme.primaryButton("CREATE ACCOUNT");
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(400, 44));
        registerBtn.addActionListener(this::onRegister);

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setBackground(AppTheme.BG_CARD);
        backRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel already = AppTheme.subLabel("Already have an account? ");
        JButton backBtn = AppTheme.linkButton("Sign In →");
        backBtn.setForeground(AppTheme.ACCENT_GOLD);
        backBtn.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_LOGIN));
        backRow.add(already);
        backRow.add(backBtn);

        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(successLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(backRow);

        outer.add(card, new GridBagConstraints());
        return outer;
    }

    // ── Form Helpers ──────────────────────────────────────────────────────────

    private void addSection(JPanel card, String title) {
        card.add(Box.createVerticalStrut(20));
        JLabel lbl = AppTheme.accentLabel("▸  " + title);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(AppTheme.redSeparator());
        card.add(Box.createVerticalStrut(12));
    }

    private JTextField addField(JPanel card, String label, String placeholder) {
        JLabel lbl = AppTheme.accentLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField tf = AppTheme.textField();
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setMaximumSize(new Dimension(400, 40));
        tf.setToolTipText(placeholder);
        card.add(lbl);
        card.add(Box.createVerticalStrut(5));
        card.add(tf);
        card.add(Box.createVerticalStrut(14));
        return tf;
    }

    private JPasswordField addPasswordField(JPanel card, String label, String placeholder) {
        JLabel lbl = AppTheme.accentLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField pf = AppTheme.passwordField();
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        pf.setMaximumSize(new Dimension(400, 40));
        pf.setToolTipText(placeholder);
        card.add(lbl);
        card.add(Box.createVerticalStrut(5));
        card.add(pf);
        card.add(Box.createVerticalStrut(14));
        return pf;
    }

    // ── Action ────────────────────────────────────────────────────────────────

    private void onRegister(ActionEvent e) {
        errorLabel.setText(" ");
        successLabel.setText(" ");

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmField.getPassword());
        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        String address  = streetField.getText().trim() + ", " +
                          cityField.getText().trim() + ", " +
                          stateField.getText().trim() + " - " +
                          pincodeField.getText().trim();

        String error = authService.register(username, password, confirm,
                fullName, email, phone, address);

        if (error == null) {
            successLabel.setText("✓  Account created! You can now sign in.");
            clearFields();
            // Auto-redirect to login after short delay
            Timer t = new Timer(2000, ev -> mainFrame.showCard(MainFrame.CARD_LOGIN));
            t.setRepeats(false);
            t.start();
        } else {
            errorLabel.setText("✗  " + error);
        }
    }

    private void clearFields() {
        usernameField.setText(""); fullNameField.setText("");
        emailField.setText("");   phoneField.setText("");
        streetField.setText("");  cityField.setText("");
        stateField.setText("");   pincodeField.setText("");
        passwordField.setText(""); confirmField.setText("");
    }
}
