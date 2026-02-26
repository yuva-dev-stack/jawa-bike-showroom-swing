package com.jawa.showroom.util;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * General-purpose formatting and ID-generation utilities.
 */
public class FormatUtil {

    private static final NumberFormat INR_FORMAT =
            NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private FormatUtil() {}

    /** Formats a double as Indian Rupee currency string. */
    public static String formatINR(double amount) {
        return INR_FORMAT.format(amount);
    }

    /** Returns current date-time as a formatted string. */
    public static String now() {
        return LocalDateTime.now().format(DATE_TIME_FMT);
    }

    /** Returns current date as a formatted string. */
    public static String today() {
        return LocalDateTime.now().format(DATE_FMT);
    }

    /** Generates a short unique booking ID like BK-A1B2C3. */
    public static String generateBookingId() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "BK-" + uuid.substring(0, 8);
    }

    /**
     * Prints a divider line of the given character repeated n times.
     */
    public static String divider(char c, int n) {
        return String.valueOf(c).repeat(n);
    }

    /**
     * Centers text within a given width by padding spaces.
     */
    public static String center(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text + " ".repeat(width - text.length() - pad);
    }
}
