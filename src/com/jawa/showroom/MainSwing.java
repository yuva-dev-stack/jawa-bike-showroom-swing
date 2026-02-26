package com.jawa.showroom;

import com.jawa.showroom.swing.AppTheme;
import com.jawa.showroom.swing.MainFrame;

import javax.swing.*;

/**
 * ═══════════════════════════════════════════════════════════════
 *   JAWA BIKE SHOWROOM — SWING GUI APPLICATION
 *   Version 2.0.0 | Java 17 | Swing UI
 *
 *   Launch the Swing-based showroom booking system.
 *   All original service / model / util layers reused.
 * ═══════════════════════════════════════════════════════════════
 */
public class MainSwing {

    public static void main(String[] args) {
        // Apply global dark theme overrides
        AppTheme.applyLookAndFeel();

        // Launch on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start application:\n" + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
