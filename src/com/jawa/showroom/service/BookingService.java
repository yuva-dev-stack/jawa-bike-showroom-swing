package com.jawa.showroom.service;

import com.jawa.showroom.model.Bike;
import com.jawa.showroom.model.Booking;
import com.jawa.showroom.model.User;
import com.jawa.showroom.util.FormatUtil;

/**
 * BookingService manages the creation of bookings and generates
 * GST-compliant text invoices.
 */
public class BookingService {

    private static final String SHOWROOM_NAME    = "Jawa Bikes - Authorised Dealership";
    private static final String SHOWROOM_ADDRESS = "123, Heritage Road, Pune, Maharashtra - 411001";
    private static final String SHOWROOM_GSTIN   = "27AABCJ1234A1ZS";   // Example GSTIN
    private static final String SHOWROOM_PHONE   = "+91-20-12345678";

    private final DataStore dataStore;

    public BookingService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ── Booking Creation ───────────────────────────────────────────────────────

    /**
     * Creates and persists a new booking.
     *
     * @param user          logged-in user
     * @param bike          the bike being booked
     * @param emiChosen     true if EMI payment selected
     * @param downPayment   down payment amount (if EMI)
     * @param annualRate    annual interest rate (if EMI)
     * @param tenureMonths  loan tenure in months (if EMI)
     * @return the created Booking object
     */
    public Booking createBooking(User user, Bike bike,
                                 boolean emiChosen, double downPayment,
                                 double annualRate, int tenureMonths) {
        Booking bk = new Booking();

        // IDs & dates
        bk.setBookingId(FormatUtil.generateBookingId());
        bk.setUsername(user.getUsername());
        bk.setBookingDate(FormatUtil.now());
        bk.setStatus("CONFIRMED");

        // Bike snapshot
        bk.setBikeId(bike.getBikeId());
        bk.setBikeModelName(bike.getModelName());
        bk.setBikeVariant(bike.getVariant());
        bk.setBikeColor(bike.getColor());

        // Pricing snapshot
        bk.setExShowroomPrice(bike.getExShowroomPrice());
        bk.setGstAmount(bike.getGstAmount());
        bk.setRtoCharges(bike.getRtoCharges());
        bk.setInsurancePremium(bike.getInsurancePremium());
        bk.setHandlingCharges(bike.getHandlingCharges());
        bk.setTotalOnRoadPrice(bike.getOnRoadPrice());

        // Customer info
        bk.setCustomerName(user.getFullName());
        bk.setCustomerEmail(user.getEmail());
        bk.setCustomerPhone(user.getPhone());
        bk.setCustomerAddress(user.getAddress());

        // EMI details
        bk.setEmiChosen(emiChosen);
        if (emiChosen) {
            double loanAmount = bike.getOnRoadPrice() - downPayment;
            double emi        = EMICalculator.calculateEMI(loanAmount, annualRate, tenureMonths);
            bk.setDownPayment(downPayment);
            bk.setLoanAmount(loanAmount);
            bk.setInterestRate(annualRate);
            bk.setTenureMonths(tenureMonths);
            bk.setEmiAmount(emi);
        }

        dataStore.saveBooking(bk);
        return bk;
    }

    // ── Invoice Generation ─────────────────────────────────────────────────────

    /**
     * Generates a formatted GST invoice string for the given booking.
     * This can be printed to console or written to a file.
     */
    public String generateInvoice(Booking bk) {
        String W = "=".repeat(64);
        String D = "-".repeat(64);
        StringBuilder sb = new StringBuilder();

        sb.append("\n").append(W).append("\n");
        sb.append(FormatUtil.center(SHOWROOM_NAME, 64)).append("\n");
        sb.append(FormatUtil.center(SHOWROOM_ADDRESS, 64)).append("\n");
        sb.append(FormatUtil.center("Ph: " + SHOWROOM_PHONE, 64)).append("\n");
        sb.append(FormatUtil.center("GSTIN: " + SHOWROOM_GSTIN, 64)).append("\n");
        sb.append(W).append("\n");
        sb.append(FormatUtil.center("TAX INVOICE / BOOKING CONFIRMATION", 64)).append("\n");
        sb.append(W).append("\n\n");

        // Booking details
        sb.append(String.format("  Booking ID   : %s%n", bk.getBookingId()));
        sb.append(String.format("  Booking Date : %s%n", bk.getBookingDate()));
        sb.append(String.format("  Status       : %s%n", bk.getStatus()));
        sb.append("\n").append(D).append("\n");

        // Customer details
        sb.append("  CUSTOMER DETAILS\n").append(D).append("\n");
        sb.append(String.format("  Name         : %s%n", bk.getCustomerName()));
        sb.append(String.format("  Email        : %s%n", bk.getCustomerEmail()));
        sb.append(String.format("  Phone        : %s%n", bk.getCustomerPhone()));
        sb.append(String.format("  Address      : %s%n", bk.getCustomerAddress()));
        sb.append("\n").append(D).append("\n");

        // Vehicle details
        sb.append("  VEHICLE DETAILS\n").append(D).append("\n");
        sb.append(String.format("  Model        : %s %s%n", bk.getBikeModelName(), bk.getBikeVariant()));
        sb.append(String.format("  Bike ID      : %s%n", bk.getBikeId()));
        sb.append(String.format("  Colour       : %s%n", bk.getBikeColor()));
        sb.append("\n").append(D).append("\n");

        // Price breakdown
        sb.append("  PRICE BREAKDOWN\n").append(D).append("\n");
        sb.append(String.format("  %-35s %s%n", "Ex-Showroom Price:",
                FormatUtil.formatINR(bk.getExShowroomPrice())));
        sb.append(String.format("  %-35s %s%n", "GST (28%):",
                FormatUtil.formatINR(bk.getGstAmount())));
        sb.append(String.format("  %-35s %s%n", "RTO Registration Charges:",
                FormatUtil.formatINR(bk.getRtoCharges())));
        sb.append(String.format("  %-35s %s%n", "Insurance Premium:",
                FormatUtil.formatINR(bk.getInsurancePremium())));
        sb.append(String.format("  %-35s %s%n", "Handling / Logistics Charges:",
                FormatUtil.formatINR(bk.getHandlingCharges())));
        sb.append(D).append("\n");
        sb.append(String.format("  %-35s %s%n", "TOTAL ON-ROAD PRICE:",
                FormatUtil.formatINR(bk.getTotalOnRoadPrice())));
        sb.append(D).append("\n");

        // EMI details (if applicable)
        if (bk.isEmiChosen()) {
            sb.append("\n  EMI PAYMENT PLAN\n").append(D).append("\n");
            sb.append(String.format("  %-35s %s%n", "Down Payment:",
                    FormatUtil.formatINR(bk.getDownPayment())));
            sb.append(String.format("  %-35s %s%n", "Loan Amount:",
                    FormatUtil.formatINR(bk.getLoanAmount())));
            sb.append(String.format("  %-35s %.2f%% p.a.%n", "Interest Rate:", bk.getInterestRate()));
            sb.append(String.format("  %-35s %d months%n", "Tenure:", bk.getTenureMonths()));
            sb.append(String.format("  %-35s %s / month%n", "Monthly EMI:",
                    FormatUtil.formatINR(bk.getEmiAmount())));
            double totalPay = EMICalculator.totalPayable(bk.getEmiAmount(), bk.getTenureMonths());
            double interest = EMICalculator.totalInterest(bk.getEmiAmount(), bk.getTenureMonths(), bk.getLoanAmount());
            sb.append(String.format("  %-35s %s%n", "Total Payable (incl. interest):",
                    FormatUtil.formatINR(bk.getDownPayment() + totalPay)));
            sb.append(String.format("  %-35s %s%n", "Total Interest Cost:",
                    FormatUtil.formatINR(interest)));
            sb.append(D).append("\n");
        }

        sb.append("\n");
        sb.append(FormatUtil.center("Thank you for choosing Jawa!", 64)).append("\n");
        sb.append(FormatUtil.center("Your legendary ride awaits.", 64)).append("\n");
        sb.append("\n").append(W).append("\n");

        return sb.toString();
    }
}
