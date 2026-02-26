package com.jawa.showroom.model;

/**
 * Represents a registered user in the Jawa Bike Showroom system.
 * Stores authentication credentials and personal details.
 */
public class User {

    private String username;
    private String passwordHash; // Stores BCrypt/SHA-256 hash, never plain text
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String createdAt;

    public User() {}

    public User(String username, String passwordHash, String fullName,
                String email, String phone, String address, String createdAt) {
        this.username    = username;
        this.passwordHash = passwordHash;
        this.fullName    = fullName;
        this.email       = email;
        this.phone       = phone;
        this.address     = address;
        this.createdAt   = createdAt;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getUsername()            { return username; }
    public void   setUsername(String v)    { this.username = v; }

    public String getPasswordHash()        { return passwordHash; }
    public void   setPasswordHash(String v){ this.passwordHash = v; }

    public String getFullName()            { return fullName; }
    public void   setFullName(String v)    { this.fullName = v; }

    public String getEmail()               { return email; }
    public void   setEmail(String v)       { this.email = v; }

    public String getPhone()               { return phone; }
    public void   setPhone(String v)       { this.phone = v; }

    public String getAddress()             { return address; }
    public void   setAddress(String v)     { this.address = v; }

    public String getCreatedAt()           { return createdAt; }
    public void   setCreatedAt(String v)   { this.createdAt = v; }

    @Override
    public String toString() {
        return "User{username='" + username + "', fullName='" + fullName +
               "', email='" + email + "', phone='" + phone + "'}";
    }
}
