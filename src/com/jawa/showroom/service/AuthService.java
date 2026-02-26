package com.jawa.showroom.service;

import com.jawa.showroom.model.User;
import com.jawa.showroom.util.FormatUtil;
import com.jawa.showroom.util.SecurityUtil;

/**
 * AuthService handles user registration, login, and session management.
 * Delegates persistence to DataStore and security to SecurityUtil.
 */
public class AuthService {

    private final DataStore dataStore;
    private User currentUser = null;   // Currently logged-in user (session)

    public AuthService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ── Registration ───────────────────────────────────────────────────────────

    /**
     * Registers a new user after validating all input fields.
     *
     * @return null on success, or an error message string on failure
     */
    public String register(String username, String password, String confirmPassword,
                           String fullName, String email, String phone, String address) {

        // Input validation
        if (username == null || username.trim().length() < 4)
            return "Username must be at least 4 characters.";

        if (!username.matches("[a-zA-Z0-9_]+"))
            return "Username can only contain letters, digits, and underscores.";

        if (dataStore.userExists(username.trim()))
            return "Username '" + username.trim() + "' is already taken.";

        if (!SecurityUtil.isStrongPassword(password))
            return "Password must be at least 8 characters and contain both letters and digits.";

        if (!password.equals(confirmPassword))
            return "Passwords do not match.";

        if (fullName == null || fullName.trim().isEmpty())
            return "Full name cannot be empty.";

        if (!SecurityUtil.isValidEmail(email))
            return "Please enter a valid email address.";

        if (!SecurityUtil.isValidPhone(phone))
            return "Please enter a valid 10-digit Indian mobile number.";

        // Hash password and persist
        String salt       = SecurityUtil.generateSalt();
        String storedHash = SecurityUtil.hashPassword(password, salt);

        User newUser = new User(
                username.trim().toLowerCase(),
                storedHash,
                fullName.trim(),
                email.trim().toLowerCase(),
                phone.trim(),
                address == null ? "" : address.trim(),
                FormatUtil.now()
        );

        dataStore.saveUser(newUser);
        return null; // success
    }

    // ── Login ──────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user and starts a session.
     *
     * @return null on success, or an error message string on failure
     */
    public String login(String username, String password) {
        if (username == null || password == null)
            return "Username and password are required.";

        User user = dataStore.findUser(username.trim().toLowerCase());
        if (user == null)
            return "No account found with username '" + username.trim() + "'.";

        if (!SecurityUtil.verifyPassword(password, user.getPasswordHash()))
            return "Incorrect password. Please try again.";

        this.currentUser = user;
        return null; // success
    }

    // ── Session ────────────────────────────────────────────────────────────────

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
