package com.jawa.showroom.swing;

import com.jawa.showroom.service.EMICalculator;
import com.jawa.showroom.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * EMIPanel — interactive EMI calculator with live result cards and a
 * custom pie chart showing principal vs interest split.
 */
public class EMIPanel extends JPanel {

    // Spinners
    private JSpinner principalSpin, downPaySpin, rateSpin, tenureSpin;

    // Result labels
    private JLabel emiLabel, totalPayLabel, interestLabel, totalCostLabel;

    // Chart panel
    private PieChartPanel pieChart;

    // Amortisation area
    private JTextArea amortArea;

    public EMIPanel() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_DARK);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BG_DARK);
        header.setBorder(new EmptyBorder(28, 32, 16, 32));

        JLabel title = AppTheme.titleLabel("📊  EMI Calculator");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = AppTheme.subLabel("Calculate your monthly instalment and total interest cost instantly.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        header.add(Box.createVerticalStrut(12));
        header.add(AppTheme.redSeparator());
        add(header, BorderLayout.NORTH);

        // ── Main split: left inputs | right results ────────────────────────
        JPanel main = new JPanel(new GridLayout(1, 2, 20, 0));
        main.setBackground(AppTheme.BG_DARK);
        main.setBorder(new EmptyBorder(16, 32, 0, 32));

        main.add(buildInputCard());
        main.add(buildResultCard());
        add(main, BorderLayout.CENTER);

        // ── Bottom: amortisation schedule ─────────────────────────────────
        JPanel bottom = buildAmortPanel();
        add(bottom, BorderLayout.SOUTH);

        // Initial calculation
        recalculate();
    }

    // ── Input Card ────────────────────────────────────────────────────────────

    private JPanel buildInputCard() {
        JPanel card = AppTheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel heading = AppTheme.headingLabel("Loan Parameters");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(heading);
        card.add(Box.createVerticalStrut(6));
        card.add(AppTheme.redSeparator());
        card.add(Box.createVerticalStrut(16));

        principalSpin = addSpinner(card, "Vehicle On-Road Price (₹)", 50000, 5000000, 10000, 250000);
        downPaySpin   = addSpinner(card, "Down Payment (₹)",           0,    5000000, 5000,  50000);
        rateSpin      = addSpinner(card, "Annual Interest Rate (%)",   1,    36,      0.25,  9.0);
        tenureSpin    = addSpinner(card, "Loan Tenure (Months)",       6,    360,     6,     36);

        ChangeListener cl = e -> recalculate();
        principalSpin.addChangeListener(cl);
        downPaySpin.addChangeListener(cl);
        rateSpin.addChangeListener(cl);
        tenureSpin.addChangeListener(cl);

        // Quick tenure shortcuts
        card.add(Box.createVerticalStrut(10));
        JLabel quickLbl = AppTheme.accentLabel("QUICK TENURE");
        quickLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(quickLbl);
        card.add(Box.createVerticalStrut(6));

        JPanel shortcuts = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        shortcuts.setBackground(AppTheme.BG_CARD);
        shortcuts.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int mo : new int[]{12, 24, 36, 48, 60}) {
            JButton btn = new JButton(mo + "M");
            btn.setFont(AppTheme.FONT_SMALL);
            btn.setForeground(AppTheme.TEXT_SECONDARY);
            btn.setBackground(new Color(40, 40, 55));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(AppTheme.BORDER_SUBTLE, 1, true),
                    new EmptyBorder(4, 10, 4, 10)));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> { tenureSpin.setValue(mo); recalculate(); });
            shortcuts.add(btn);
        }
        card.add(shortcuts);

        card.add(Box.createVerticalGlue());
        return card;
    }

    // ── Result Card ───────────────────────────────────────────────────────────

    private JPanel buildResultCard() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(AppTheme.BG_DARK);

        // Result numbers card
        JPanel card = AppTheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = AppTheme.headingLabel("Results");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(heading);
        card.add(Box.createVerticalStrut(6));
        card.add(AppTheme.redSeparator());
        card.add(Box.createVerticalStrut(16));

        emiLabel       = resultBlock(card, "Monthly EMI",               AppTheme.ACCENT_GOLD,   true);
        totalPayLabel  = resultBlock(card, "Total Loan Payable",        AppTheme.TEXT_PRIMARY,  false);
        interestLabel  = resultBlock(card, "Total Interest Cost",       AppTheme.TEXT_ERROR,    false);
        totalCostLabel = resultBlock(card, "Total Outflow (Down+Loan)", AppTheme.TEXT_SUCCESS,  false);

        outer.add(card);
        outer.add(Box.createVerticalStrut(14));

        // Pie chart
        pieChart = new PieChartPanel();
        pieChart.setAlignmentX(Component.LEFT_ALIGNMENT);
        pieChart.setPreferredSize(new Dimension(0, 180));
        pieChart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        outer.add(pieChart);

        return outer;
    }

    // ── Amortisation Panel ────────────────────────────────────────────────────

    private JPanel buildAmortPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.BG_DARK);
        p.setBorder(new EmptyBorder(12, 32, 24, 32));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(AppTheme.BG_DARK);
        JLabel lbl = AppTheme.accentLabel("AMORTISATION SCHEDULE");
        JButton calcBtn = AppTheme.secondaryButton("Generate Schedule");
        calcBtn.addActionListener(e -> generateAmortisation());
        headerRow.add(lbl,     BorderLayout.WEST);
        headerRow.add(calcBtn, BorderLayout.EAST);
        p.add(headerRow, BorderLayout.NORTH);

        amortArea = new JTextArea(6, 0);
        amortArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        amortArea.setForeground(AppTheme.TEXT_PRIMARY);
        amortArea.setBackground(AppTheme.BG_CARD);
        amortArea.setEditable(false);
        amortArea.setBorder(new EmptyBorder(10, 14, 10, 14));
        amortArea.setText("  Click 'Generate Schedule' to see the month-by-month breakdown.");

        JScrollPane scroll = new JScrollPane(amortArea);
        scroll.setBorder(new LineBorder(AppTheme.BORDER_SUBTLE, 1));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Calculation ───────────────────────────────────────────────────────────

    private void recalculate() {
        double principal = ((Number) principalSpin.getValue()).doubleValue();
        double downPay   = ((Number) downPaySpin.getValue()).doubleValue();
        double rate      = ((Number) rateSpin.getValue()).doubleValue();
        int    tenure    = ((Number) tenureSpin.getValue()).intValue();

        double loanAmt   = Math.max(0, principal - downPay);
        double emi       = EMICalculator.calculateEMI(loanAmt, rate, tenure);
        double totalPay  = EMICalculator.totalPayable(emi, tenure);
        double interest  = EMICalculator.totalInterest(emi, tenure, loanAmt);
        double totalOut  = downPay + totalPay;

        emiLabel.setText(FormatUtil.formatINR(emi));
        totalPayLabel.setText(FormatUtil.formatINR(totalPay));
        interestLabel.setText(FormatUtil.formatINR(interest));
        totalCostLabel.setText(FormatUtil.formatINR(totalOut));

        pieChart.setValues(loanAmt, interest, downPay);
    }

    private void generateAmortisation() {
        double principal = ((Number) principalSpin.getValue()).doubleValue();
        double downPay   = ((Number) downPaySpin.getValue()).doubleValue();
        double rate      = ((Number) rateSpin.getValue()).doubleValue();
        int    tenure    = ((Number) tenureSpin.getValue()).intValue();
        double loanAmt   = Math.max(0, principal - downPay);

        amortArea.setText(EMICalculator.amortisationTable(loanAmt, rate, tenure));
        amortArea.setCaretPosition(0);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JSpinner addSpinner(JPanel card, String label,
                                double min, double max, double step, double init) {
        JLabel lbl = AppTheme.accentLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSpinner sp = AppTheme.spinner(min, max, step, init);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(lbl);
        card.add(Box.createVerticalStrut(5));
        card.add(sp);
        card.add(Box.createVerticalStrut(14));
        return sp;
    }

    private JLabel resultBlock(JPanel card, String title, Color valueColor, boolean large) {
        JLabel titleLbl = AppTheme.subLabel(title);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLbl = new JLabel("₹0.00");
        valueLbl.setFont(large ? AppTheme.FONT_TITLE : AppTheme.FONT_HEADING);
        valueLbl.setForeground(valueColor);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLbl);
        card.add(valueLbl);
        card.add(Box.createVerticalStrut(12));
        return valueLbl;
    }

    // ── Inner Pie Chart ───────────────────────────────────────────────────────

    static class PieChartPanel extends JPanel {
        private double principal = 1, interest = 0, downPay = 0;

        PieChartPanel() {
            setBackground(AppTheme.BG_CARD);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(AppTheme.BORDER_SUBTLE, 1, true),
                    new EmptyBorder(10, 10, 10, 10)));
        }

        void setValues(double p, double i, double d) {
            this.principal = p;
            this.interest  = i;
            this.downPay   = d;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int size = Math.min(w / 2 - 20, h - 30);
            int x = 20, y = (h - size) / 2;

            double total = principal + interest + downPay;
            if (total <= 0) return;

            double[] slices = {downPay, principal, interest};
            Color[]  colors = {AppTheme.ACCENT_GOLD, new Color(80,120,200), AppTheme.TEXT_ERROR};
            String[] labels = {"Down Payment", "Principal", "Interest"};

            int startAngle = 90;
            for (int i = 0; i < slices.length; i++) {
                int arc = (int) Math.round(slices[i] / total * 360);
                g2.setColor(colors[i]);
                g2.fillArc(x, y, size, size, startAngle, -arc);
                g2.setColor(AppTheme.BG_CARD);
                g2.setStroke(new BasicStroke(2));
                g2.drawArc(x, y, size, size, startAngle, -arc);
                startAngle -= arc;
            }

            // Legend
            int lx = x + size + 24, ly = y + 20;
            for (int i = 0; i < labels.length; i++) {
                g2.setColor(colors[i]);
                g2.fillRoundRect(lx, ly + i * 36, 14, 14, 4, 4);
                g2.setColor(AppTheme.TEXT_PRIMARY);
                g2.setFont(AppTheme.FONT_SMALL);
                g2.drawString(labels[i], lx + 20, ly + i * 36 + 11);
                g2.setColor(AppTheme.TEXT_SECONDARY);
                g2.drawString(String.format("%.1f%%", slices[i] / total * 100), lx + 20, ly + i * 36 + 25);
            }

            // Centre label
            g2.setColor(AppTheme.TEXT_SECONDARY);
            g2.setFont(AppTheme.FONT_SMALL);
            String centre = "Split";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(centre, x + size / 2 - fm.stringWidth(centre) / 2, y + size / 2 + 4);
        }
    }
}
