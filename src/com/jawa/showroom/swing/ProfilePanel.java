package com.jawa.showroom.swing;

import com.jawa.showroom.model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * ProfilePanel — displays the logged-in user's account details.
 */
public class ProfilePanel extends JPanel {

    private final User user;

    public ProfilePanel(User user) {
        this.user = user;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_DARK);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BG_DARK);
        header.setBorder(new EmptyBorder(28, 32, 16, 32));

        JLabel title = AppTheme.titleLabel("👤  My Profile");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = AppTheme.subLabel("Your account details registered with Jawa Showroom.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        header.add(Box.createVerticalStrut(12));
        header.add(AppTheme.redSeparator());
        add(header, BorderLayout.NORTH);

        // ── Content ───────────────────────────────────────────────────────────
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(AppTheme.BG_DARK);
        centre.setBorder(new EmptyBorder(24, 32, 32, 32));

        JPanel card = buildProfileCard();
        centre.add(card, new GridBagConstraints());
        add(centre, BorderLayout.CENTER);
    }

    private JPanel buildProfileCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(30, 36, 30, 36)));

        // Avatar circle
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.ACCENT_RED);
                g2.fillOval(0, 0, 72, 72);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 30));
                String initials = getInitials(user.getFullName());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials,
                        36 - fm.stringWidth(initials) / 2,
                        36 + fm.getAscent() / 2 - 2);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(72, 72));
        avatar.setMaximumSize(new Dimension(72, 72));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(avatar);
        card.add(Box.createVerticalStrut(12));

        JLabel name = new JLabel(user.getFullName());
        name.setFont(AppTheme.FONT_TITLE);
        name.setForeground(AppTheme.TEXT_PRIMARY);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(name);

        JLabel unameLabel = new JLabel("@" + user.getUsername());
        unameLabel.setFont(AppTheme.FONT_BODY);
        unameLabel.setForeground(AppTheme.ACCENT_GOLD);
        unameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(unameLabel);

        card.add(Box.createVerticalStrut(20));
        card.add(AppTheme.redSeparator());
        card.add(Box.createVerticalStrut(20));

        // Details grid
        JPanel grid = new JPanel(new GridLayout(0, 2, 20, 14));
        grid.setBackground(AppTheme.BG_CARD);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        addRow(grid, "Email",        user.getEmail());
        addRow(grid, "Mobile",       user.getPhone());
        addRow(grid, "Address",      user.getAddress());
        addRow(grid, "Member Since", user.getCreatedAt());

        card.add(grid);
        return card;
    }

    private void addRow(JPanel grid, String label, String value) {
        JLabel k = AppTheme.accentLabel(label);
        JLabel v = AppTheme.subLabel(value);
        v.setForeground(AppTheme.TEXT_PRIMARY);
        grid.add(k);
        grid.add(v);
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return String.valueOf(parts[0].charAt(0)).toUpperCase() +
               String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
