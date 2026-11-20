package com.pgms.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** Small date helpers. Dates are typed and shown as yyyy-MM-dd, months as yyyy-MM. */
public final class Dates {

    private Dates() {
    }

    public static String format(Date d) {
        return d == null ? "-" : new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    public static java.sql.Date parse(String text) throws ParseException {
        String t = text.trim();
        // SimpleDateFormat ignores junk after the date ("2026-11-20xyz"), so check the shape first
        if (!t.matches("(19|20)[0-9]{2}-[0-9]{2}-[0-9]{2}")) {
            throw new ParseException(t, 0);
        }
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        f.setLenient(false);
        return new java.sql.Date(f.parse(t).getTime());
    }

    public static java.sql.Date today() {
        return new java.sql.Date(System.currentTimeMillis());
    }

    public static String monthOf(Date d) {
        return new SimpleDateFormat("yyyy-MM").format(d);
    }

    public static String currentMonth() {
        return monthOf(new Date());
    }

    /** "2026-11" -> "2026-12" */
    public static String nextMonth(String month) {
        Calendar c = toCalendar(month);
        c.add(Calendar.MONTH, 1);
        return new SimpleDateFormat("yyyy-MM").format(c.getTime());
    }

    /** Compare two "yyyy-MM" strings; this ordering works on the text directly. */
    public static int compareMonths(String a, String b) {
        return a.compareTo(b);
    }

    private static Calendar toCalendar(String month) {
        String[] parts = month.split("-");
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, 1);
        return c;
    }
}
