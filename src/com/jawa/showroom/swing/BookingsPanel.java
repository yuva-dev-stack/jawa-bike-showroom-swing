package com.jawa.showroom.swing;

import com.jawa.showroom.model.Booking;
import com.jawa.showroom.model.User;
import com.jawa.showroom.service.BookingService;
import com.jawa.showroom.service.DataStore;
import com.jawa.showroom.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * BookingsPanel — lists all of the current user's bookings in a table,
 * and allows reprinting / saving any GST invoice.
 *
 * FIX: replaced SwingUtilities.getWindowAncestor(this) with a safe
 *      helper that finds the parent Frame after the panel is fully shown.
 */
public class BookingsPanel extends JPanel {

    private final DataStore      dataStore;
    private final BookingService bookingService;
    private final User           currentUser;

    private JTable            table;
    private DefaultTableModel model;
    private List<Booking>     currentBookings;

    // Summary labels
    private JLabel totalBookingsLbl, totalSpentLbl;

    public BookingsPanel(DataStore dataStore, BookingService bookingService, User currentUser) {
        this.dataStore      = dataStore;
        this.bookingService = bookingService;
        this.currentUser    = currentUser;
        buildUI();
    }

    // ── Safe parent-window helper ──────────────────────────────────────────────
    /**
     * Returns the parent Frame safely. Works even before the panel is
     * fully added to a window hierarchy, falling back to the active frame.
     */
    private Frame getParentFrame() {
        Window w = SwingUtilities.windowForComponent(this);
        if (w instanceof Frame f) return f;
        // Fallback: use the first visible JFrame
        for (Frame f : Frame.getFrames()) {
            if (f.isVisible()) return f;
        }
        return null;
    }

    // ── UI Construction ────────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_DARK);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BG_DARK);
        header.setBorder(new EmptyBorder(28, 32, 16, 32));

        JLabel title = AppTheme.titleLabel("📋  My Bookings");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = AppTheme.subLabel("All your bike bookings and invoices in one place.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        header.add(Box.createVerticalStrut(12));
        header.add(AppTheme.redSeparator());
        add(header, BorderLayout.NORTH);

        // ── Summary strip ─────────────────────────────────────────────────────
        JPanel summary = buildSummaryStrip();
        add(summary, BorderLayout.SOUTH);

        // ── Table ─────────────────────────────────────────────────────────────
        JPanel tablePanel = buildTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildSummaryStrip() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 32, 12));
        p.setBackground(AppTheme.BG_CARD);
        p.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER_SUBTLE));

        totalBookingsLbl = summaryBlock(p, "Total Bookings", "0");
        totalSpentLbl    = summaryBlock(p, "Total Spent",    "₹0");

        JButton reprintBtn = AppTheme.secondaryButton("🖨  View / Reprint Invoice");
        reprintBtn.addActionListener(e -> reprintSelectedInvoice());
        p.add(reprintBtn);

        return p;
    }

    private JLabel summaryBlock(JPanel parent, String title, String initValue) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBackground(AppTheme.BG_CARD);

        JLabel titleLbl = AppTheme.subLabel(title);
        JLabel valueLbl = new JLabel(initValue);
        valueLbl.setFont(AppTheme.FONT_HEADING);
        valueLbl.setForeground(AppTheme.ACCENT_GOLD);

        block.add(titleLbl);
        block.add(valueLbl);
        parent.add(block);
        return valueLbl;
    }

    private JPanel buildTablePanel() {
        String[] cols = {"Booking ID", "Date", "Bike Model", "Variant", "Colour",
                         "On-Road Price", "Payment", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        AppTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new StripedRenderer());

        int[] widths = {110, 140, 120, 130, 100, 120, 80, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.setBackground(AppTheme.BG_DARK);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.BG_DARK);
        p.setBorder(new EmptyBorder(0, 32, 0, 32));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Refresh Data ──────────────────────────────────────────────────────────

    public void refresh() {
        model.setRowCount(0);
        currentBookings = dataStore.getBookingsByUser(currentUser.getUsername());

        double totalSpent = 0;
        for (Booking bk : currentBookings) {
            model.addRow(new Object[]{
                bk.getBookingId(),
                bk.getBookingDate(),
                bk.getBikeModelName(),
                bk.getBikeVariant(),
                bk.getBikeColor(),
                FormatUtil.formatINR(bk.getTotalOnRoadPrice()),
                bk.isEmiChosen() ? "EMI" : "Cash",
                bk.getStatus()
            });
            totalSpent += bk.getTotalOnRoadPrice();
        }

        totalBookingsLbl.setText(String.valueOf(currentBookings.size()));
        totalSpentLbl.setText(FormatUtil.formatINR(totalSpent));
    }

    // ── Invoice Reprint ───────────────────────────────────────────────────────

    private void reprintSelectedInvoice() {
        int row = table.getSelectedRow();
        if (row < 0 || currentBookings == null || currentBookings.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a booking from the table first.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Booking bk = currentBookings.get(row);
        showInvoiceDialog(bookingService.generateInvoice(bk), bk.getBookingId());
    }

    // ── Invoice Dialog — FIXED ────────────────────────────────────────────────

    private void showInvoiceDialog(String invoice, String bookingId) {
        // ✅ FIX: use getParentFrame() helper instead of SwingUtilities.getWindowAncestor(this)
        //         getWindowAncestor() returned null here because BookingsPanel is a JPanel
        //         embedded in a CardLayout — the Window reference was not yet established
        //         when the method was first compiled/validated.
        JDialog dialog = new JDialog(getParentFrame(), "Invoice — " + bookingId, true);
        dialog.setSize(660, 640);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(AppTheme.BG_DARK);
        dialog.setLayout(new BorderLayout());

        // Title bar inside dialog
        JPanel dialogHeader = new JPanel(new BorderLayout());
        dialogHeader.setBackground(AppTheme.ACCENT_RED);
        dialogHeader.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel dialogTitle = new JLabel("GST Tax Invoice / Booking Confirmation");
        dialogTitle.setFont(AppTheme.FONT_SUBHEAD);
        dialogTitle.setForeground(Color.WHITE);
        JLabel dialogId = new JLabel(bookingId);
        dialogId.setFont(AppTheme.FONT_SMALL);
        dialogId.setForeground(new Color(255, 220, 220));
        dialogHeader.add(dialogTitle, BorderLayout.WEST);
        dialogHeader.add(dialogId,    BorderLayout.EAST);
        dialog.add(dialogHeader, BorderLayout.NORTH);

        // Invoice text area
        JTextArea area = new JTextArea(invoice);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setForeground(AppTheme.TEXT_PRIMARY);
        area.setBackground(AppTheme.BG_CARD);
        area.setEditable(false);
        area.setBorder(new EmptyBorder(16, 20, 16, 20));
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        dialog.add(scroll, BorderLayout.CENTER);

        // Bottom button bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setBackground(AppTheme.BG_DARK);
        bottom.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER_SUBTLE));

        JButton saveBtn = AppTheme.secondaryButton("💾  Save to File");
        saveBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("Invoice_" + bookingId + ".txt"));
            if (fc.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try (java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile())) {
                    pw.print(invoice);
                    JOptionPane.showMessageDialog(dialog, "✓  Invoice saved successfully!",
                            "Saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Could not save: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton closeBtn = AppTheme.primaryButton("  Close  ");
        closeBtn.addActionListener(e -> dialog.dispose());

        bottom.add(saveBtn);
        bottom.add(closeBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ── Striped Table Renderer ────────────────────────────────────────────────

    static class StripedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, selected, focus, row, col);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            if (selected) {
                setBackground(AppTheme.ACCENT_RED);
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? AppTheme.BG_CARD : AppTheme.BG_TABLE_ALT);
                setForeground(AppTheme.TEXT_PRIMARY);
            }
            if (col == 7 && !selected) {
                String val = v == null ? "" : v.toString();
                setForeground("CONFIRMED".equals(val) ? AppTheme.TEXT_SUCCESS :
                              "CANCELLED".equals(val) ? AppTheme.TEXT_ERROR   : AppTheme.TEXT_SECONDARY);
                setFont(AppTheme.FONT_SUBHEAD);
            }
            return this;
        }
    }
}
