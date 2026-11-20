package com.pgms.util;

/** Formats rupee amounts the Indian way: 1,23,456 (lakh / crore grouping). */
public final class Money {

    private Money() {
    }

    public static String inr(double amount) {
        long totalPaise = Math.round(amount * 100);
        boolean negative = totalPaise < 0;
        long rupees = Math.abs(totalPaise) / 100;
        long paise = Math.abs(totalPaise) % 100;
        String digits = Long.toString(rupees);

        StringBuilder out = new StringBuilder();
        int len = digits.length();
        if (len <= 3) {
            out.append(digits);
        } else {
            String lastThree = digits.substring(len - 3);
            String rest = digits.substring(0, len - 3);
            // the part before the last three digits is grouped in twos
            StringBuilder grouped = new StringBuilder();
            int count = 0;
            for (int i = rest.length() - 1; i >= 0; i--) {
                grouped.insert(0, rest.charAt(i));
                count++;
                if (count % 2 == 0 && i > 0) {
                    grouped.insert(0, ',');
                }
            }
            out.append(grouped).append(',').append(lastThree);
        }
        if (paise > 0) {
            out.append('.').append(paise < 10 ? "0" : "").append(paise);
        }
        return (negative ? "-Rs." : "Rs.") + out;
    }
}
