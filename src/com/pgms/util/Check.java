package com.pgms.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Input checks shared by the console and the GUI, so bad values are stopped
 * with a clear message before they reach MySQL.
 */
public final class Check {

    /** Largest amount the money columns (DECIMAL(10,2)) can hold, kept round: Rs.1 crore. */
    public static final double MAX_AMOUNT = 1_00_00_000;

    private Check() {
    }

    /** A rupee amount: a real number, not negative (or not zero), at most Rs.1 crore, rounded to paise. */
    public static double money(double value, boolean allowZero, String what) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(what + " must be a number");
        }
        if (value < 0 || (!allowZero && value == 0)) {
            throw new IllegalArgumentException(what + (allowZero ? " cannot be negative" : " must be more than zero"));
        }
        if (value > MAX_AMOUNT) {
            throw new IllegalArgumentException(what + " cannot be more than " + Money.inr(MAX_AMOUNT));
        }
        return paise(value);
    }

    /** Round to 2 decimal places, so sums of payments compare exactly. */
    public static double paise(double value) {
        return Math.round(value * 100) / 100.0;
    }

    /** Trimmed text no longer than the database column. Empty gives null unless required. */
    public static String text(String value, int maxLength, String what, boolean required) {
        String s = value == null ? "" : value.trim();
        if (s.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(what + " cannot be empty");
            }
            return null;
        }
        if (s.length() > maxLength) {
            throw new IllegalArgumentException(what + " can be at most " + maxLength + " characters (it has " + s.length() + ")");
        }
        return s;
    }

    public static String email(String value) {
        String s = text(value, 80, "Email", false);
        if (s != null && !s.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Email does not look right (example: name@gmail.com)");
        }
        return s;
    }

    /** The date must not be after today. */
    public static void notFuture(Date d, String what) {
        if (d.after(endOfToday())) {
            throw new IllegalArgumentException(what + " cannot be in the future");
        }
    }

    /** The date must not be more than the given number of days after today. */
    public static void notBeyond(Date d, int daysAhead, String what) {
        Calendar c = Calendar.getInstance();
        c.setTime(endOfToday());
        c.add(Calendar.DAY_OF_MONTH, daysAhead);
        if (d.after(c.getTime())) {
            throw new IllegalArgumentException(what + " cannot be more than " + daysAhead + " days from today");
        }
    }

    private static Date endOfToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }
}
