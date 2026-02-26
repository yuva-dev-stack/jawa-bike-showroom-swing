package com.jawa.showroom.model;

/**
 * Represents a Jawa motorcycle with full specifications and pricing details.
 * All prices stored in Indian Rupees (INR).
 */
public class Bike {

    // ── Identity ───────────────────────────────────────────────────────────────
    private String bikeId;
    private String modelName;
    private String variant;       // e.g., "Standard", "ABS"
    private String color;
    private boolean available;

    // ── Engine & Performance ───────────────────────────────────────────────────
    private String engineCC;      // e.g., "334 cc"
    private String engineType;    // e.g., "Single Cylinder, Liquid Cooled"
    private String maxPower;      // e.g., "30.64 PS @ 8000 rpm"
    private String maxTorque;     // e.g., "32.74 Nm @ 6500 rpm"
    private String transmission;  // e.g., "6-Speed"
    private String fuelType;
    private String fuelTankCapacity;
    private String mileage;

    // ── Dimensions & Weight ────────────────────────────────────────────────────
    private String kerbWeight;
    private String seatHeight;
    private String wheelbase;
    private String groundClearance;

    // ── Brakes & Suspension ────────────────────────────────────────────────────
    private String frontBrake;
    private String rearBrake;
    private String frontSuspension;
    private String rearSuspension;

    // ── Pricing (INR) ─────────────────────────────────────────────────────────
    private double exShowroomPrice;
    private double rtoCharges;
    private double insurancePremium;
    private double handlingCharges;
    private double gstRate;          // GST percentage (e.g., 28.0)

    // ── Image / Description ────────────────────────────────────────────────────
    private String description;
    private String imageAscii;       // ASCII art or placeholder for CLI display

    public Bike() {}

    // ── Computed Pricing Methods ───────────────────────────────────────────────

    /**
     * Calculates GST amount on ex-showroom price.
     */
    public double getGstAmount() {
        return (exShowroomPrice * gstRate) / 100.0;
    }

    /**
     * Calculates total on-road price including all charges.
     */
    public double getOnRoadPrice() {
        return exShowroomPrice + getGstAmount() + rtoCharges + insurancePremium + handlingCharges;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getBikeId()                   { return bikeId; }
    public void   setBikeId(String v)           { this.bikeId = v; }

    public String getModelName()                { return modelName; }
    public void   setModelName(String v)        { this.modelName = v; }

    public String getVariant()                  { return variant; }
    public void   setVariant(String v)          { this.variant = v; }

    public String getColor()                    { return color; }
    public void   setColor(String v)            { this.color = v; }

    public boolean isAvailable()               { return available; }
    public void    setAvailable(boolean v)     { this.available = v; }

    public String getEngineCC()                 { return engineCC; }
    public void   setEngineCC(String v)         { this.engineCC = v; }

    public String getEngineType()               { return engineType; }
    public void   setEngineType(String v)       { this.engineType = v; }

    public String getMaxPower()                 { return maxPower; }
    public void   setMaxPower(String v)         { this.maxPower = v; }

    public String getMaxTorque()                { return maxTorque; }
    public void   setMaxTorque(String v)        { this.maxTorque = v; }

    public String getTransmission()             { return transmission; }
    public void   setTransmission(String v)     { this.transmission = v; }

    public String getFuelType()                 { return fuelType; }
    public void   setFuelType(String v)         { this.fuelType = v; }

    public String getFuelTankCapacity()         { return fuelTankCapacity; }
    public void   setFuelTankCapacity(String v) { this.fuelTankCapacity = v; }

    public String getMileage()                  { return mileage; }
    public void   setMileage(String v)          { this.mileage = v; }

    public String getKerbWeight()               { return kerbWeight; }
    public void   setKerbWeight(String v)       { this.kerbWeight = v; }

    public String getSeatHeight()               { return seatHeight; }
    public void   setSeatHeight(String v)       { this.seatHeight = v; }

    public String getWheelbase()                { return wheelbase; }
    public void   setWheelbase(String v)        { this.wheelbase = v; }

    public String getGroundClearance()          { return groundClearance; }
    public void   setGroundClearance(String v)  { this.groundClearance = v; }

    public String getFrontBrake()               { return frontBrake; }
    public void   setFrontBrake(String v)       { this.frontBrake = v; }

    public String getRearBrake()                { return rearBrake; }
    public void   setRearBrake(String v)        { this.rearBrake = v; }

    public String getFrontSuspension()          { return frontSuspension; }
    public void   setFrontSuspension(String v)  { this.frontSuspension = v; }

    public String getRearSuspension()           { return rearSuspension; }
    public void   setRearSuspension(String v)   { this.rearSuspension = v; }

    public double getExShowroomPrice()          { return exShowroomPrice; }
    public void   setExShowroomPrice(double v)  { this.exShowroomPrice = v; }

    public double getRtoCharges()               { return rtoCharges; }
    public void   setRtoCharges(double v)       { this.rtoCharges = v; }

    public double getInsurancePremium()         { return insurancePremium; }
    public void   setInsurancePremium(double v) { this.insurancePremium = v; }

    public double getHandlingCharges()          { return handlingCharges; }
    public void   setHandlingCharges(double v)  { this.handlingCharges = v; }

    public double getGstRate()                  { return gstRate; }
    public void   setGstRate(double v)          { this.gstRate = v; }

    public String getDescription()              { return description; }
    public void   setDescription(String v)      { this.description = v; }

    public String getImageAscii()               { return imageAscii; }
    public void   setImageAscii(String v)       { this.imageAscii = v; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s - On-Road: ₹%.0f", bikeId, modelName, variant, getOnRoadPrice());
    }
}
