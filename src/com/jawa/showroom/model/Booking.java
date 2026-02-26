package com.jawa.showroom.model;

/**
 * Represents a bike booking / order placed by a user.
 * Captures all details needed for invoice generation.
 */
public class Booking {

    // ── Booking Metadata ───────────────────────────────────────────────────────
    private String bookingId;
    private String username;
    private String bookingDate;
    private String status;          // CONFIRMED / CANCELLED / DELIVERED

    // ── Bike Details (snapshot at time of booking) ─────────────────────────────
    private String bikeId;
    private String bikeModelName;
    private String bikeVariant;
    private String bikeColor;

    // ── Pricing Snapshot ───────────────────────────────────────────────────────
    private double exShowroomPrice;
    private double gstAmount;
    private double rtoCharges;
    private double insurancePremium;
    private double handlingCharges;
    private double totalOnRoadPrice;

    // ── EMI Details (optional – filled if user chose EMI) ─────────────────────
    private boolean emiChosen;
    private double  loanAmount;
    private double  interestRate;    // Annual %
    private int     tenureMonths;
    private double  emiAmount;
    private double  downPayment;

    // ── Customer Info ──────────────────────────────────────────────────────────
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;

    public Booking() {}

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getBookingId()                 { return bookingId; }
    public void   setBookingId(String v)         { this.bookingId = v; }

    public String getUsername()                  { return username; }
    public void   setUsername(String v)          { this.username = v; }

    public String getBookingDate()               { return bookingDate; }
    public void   setBookingDate(String v)       { this.bookingDate = v; }

    public String getStatus()                    { return status; }
    public void   setStatus(String v)            { this.status = v; }

    public String getBikeId()                    { return bikeId; }
    public void   setBikeId(String v)            { this.bikeId = v; }

    public String getBikeModelName()             { return bikeModelName; }
    public void   setBikeModelName(String v)     { this.bikeModelName = v; }

    public String getBikeVariant()               { return bikeVariant; }
    public void   setBikeVariant(String v)       { this.bikeVariant = v; }

    public String getBikeColor()                 { return bikeColor; }
    public void   setBikeColor(String v)         { this.bikeColor = v; }

    public double getExShowroomPrice()           { return exShowroomPrice; }
    public void   setExShowroomPrice(double v)   { this.exShowroomPrice = v; }

    public double getGstAmount()                 { return gstAmount; }
    public void   setGstAmount(double v)         { this.gstAmount = v; }

    public double getRtoCharges()                { return rtoCharges; }
    public void   setRtoCharges(double v)        { this.rtoCharges = v; }

    public double getInsurancePremium()          { return insurancePremium; }
    public void   setInsurancePremium(double v)  { this.insurancePremium = v; }

    public double getHandlingCharges()           { return handlingCharges; }
    public void   setHandlingCharges(double v)   { this.handlingCharges = v; }

    public double getTotalOnRoadPrice()          { return totalOnRoadPrice; }
    public void   setTotalOnRoadPrice(double v)  { this.totalOnRoadPrice = v; }

    public boolean isEmiChosen()                 { return emiChosen; }
    public void    setEmiChosen(boolean v)       { this.emiChosen = v; }

    public double getLoanAmount()                { return loanAmount; }
    public void   setLoanAmount(double v)        { this.loanAmount = v; }

    public double getInterestRate()              { return interestRate; }
    public void   setInterestRate(double v)      { this.interestRate = v; }

    public int    getTenureMonths()              { return tenureMonths; }
    public void   setTenureMonths(int v)         { this.tenureMonths = v; }

    public double getEmiAmount()                 { return emiAmount; }
    public void   setEmiAmount(double v)         { this.emiAmount = v; }

    public double getDownPayment()               { return downPayment; }
    public void   setDownPayment(double v)       { this.downPayment = v; }

    public String getCustomerName()              { return customerName; }
    public void   setCustomerName(String v)      { this.customerName = v; }

    public String getCustomerEmail()             { return customerEmail; }
    public void   setCustomerEmail(String v)     { this.customerEmail = v; }

    public String getCustomerPhone()             { return customerPhone; }
    public void   setCustomerPhone(String v)     { this.customerPhone = v; }

    public String getCustomerAddress()           { return customerAddress; }
    public void   setCustomerAddress(String v)   { this.customerAddress = v; }

    @Override
    public String toString() {
        return String.format("Booking[%s] %s %s | ₹%.0f | %s",
                bookingId, bikeModelName, bikeVariant, totalOnRoadPrice, status);
    }
}
