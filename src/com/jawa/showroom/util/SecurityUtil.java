package com.jawa.showroom.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security utility class for password hashing and validation.
 * Uses SHA-256 with a random salt for secure password storage.
 *
 * NOTE: In production, replace with BCrypt (add bcrypt library to classpath).
 */
public class SecurityUtil {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int    SALT_BYTES      = 16;

    private SecurityUtil() { /* Utility class – no instances */ }

    /**
     * Generates a random salt encoded as Base64.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a plain-text password combined with the given salt.
     *
     * @param password  plain-text password
     * @param salt      Base64-encoded salt
     * @return          Base64-encoded hash string (salt:hash format)
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashBytes = md.digest(password.getBytes());
            String hash = Base64.getEncoder().encodeToString(hashBytes);
            return salt + ":" + hash;  // Store salt with hash for verification
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found: " + HASH_ALGORITHM, e);
        }
    }

    /**
     * Verifies a plain-text password against a stored hash (salt:hash).
     *
     * @param password    plain-text password to verify
     * @param storedHash  the stored "salt:hash" string
     * @return            true if the password matches
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) return false;
        String[] parts = storedHash.split(":", 2);
        String salt         = parts[0];
        String expectedHash = hashPassword(password, salt);
        return expectedHash.equals(storedHash);
    }

    /**
     * Validates that an email has a basic valid format.
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Validates that a phone number is a 10-digit Indian mobile number.
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[6-9]\\d{9}$");
    }

    /**
     * Validates password strength: min 8 chars, must include letter and digit.
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit  = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }
}
