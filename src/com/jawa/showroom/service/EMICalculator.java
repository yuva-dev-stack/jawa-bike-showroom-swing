package com.jawa.showroom.service;

/**
 * EMICalculator computes Equated Monthly Instalments using the standard
 * reducing-balance formula:
 *
 *   EMI = P * r * (1+r)^n / ((1+r)^n - 1)
 *
 * where:
 *   P = principal loan amount (INR)
 *   r = monthly interest rate (annual rate / 12 / 100)
 *   n = tenure in months
 */
public class EMICalculator {

    /**
     * Calculates the monthly EMI.
     *
     * @param principal       loan amount in INR
     * @param annualRatePercent annual interest rate (e.g., 9.5 for 9.5%)
     * @param tenureMonths    repayment period in months
     * @return monthly EMI in INR
     */
    public static double calculateEMI(double principal, double annualRatePercent, int tenureMonths) {
        if (principal <= 0 || tenureMonths <= 0) return 0;

        if (annualRatePercent <= 0) {
            // Zero-interest loan: simple division
            return principal / tenureMonths;
        }

        double r = annualRatePercent / 12.0 / 100.0;   // monthly rate
        double pow = Math.pow(1 + r, tenureMonths);
        return (principal * r * pow) / (pow - 1);
    }

    /**
     * Total amount repaid over full tenure (EMI × months).
     */
    public static double totalPayable(double emi, int tenureMonths) {
        return emi * tenureMonths;
    }

    /**
     * Total interest charged (totalPayable - principal).
     */
    public static double totalInterest(double emi, int tenureMonths, double principal) {
        return totalPayable(emi, tenureMonths) - principal;
    }

    /**
     * Builds a full amortisation schedule string (month-by-month breakdown).
     */
    public static String amortisationTable(double principal, double annualRatePercent, int tenureMonths) {
        double emi      = calculateEMI(principal, annualRatePercent, tenureMonths);
        double monthlyR = annualRatePercent / 12.0 / 100.0;
        double balance  = principal;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-12s %-12s %-12s %-14s%n",
                "Month", "EMI (₹)", "Principal", "Interest", "Balance (₹)"));
        sb.append("-".repeat(60)).append("\n");

        for (int m = 1; m <= tenureMonths; m++) {
            double interest  = balance * monthlyR;
            double principalPart = emi - interest;
            balance -= principalPart;
            if (balance < 0) balance = 0;

            sb.append(String.format("%-6d %-12.2f %-12.2f %-12.2f %-14.2f%n",
                    m, emi, principalPart, interest, balance));
        }

        return sb.toString();
    }
}
