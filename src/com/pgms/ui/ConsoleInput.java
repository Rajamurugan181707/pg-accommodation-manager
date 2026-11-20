package com.pgms.ui;

import com.pgms.util.Check;
import com.pgms.util.Dates;

import java.text.ParseException;
import java.util.Scanner;

/** Reads and checks keyboard input, asking again until the value is valid. */
public class ConsoleInput {

    /** Thrown when input runs out (Ctrl+D, or the end of a test script). */
    public static class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final Scanner in = new Scanner(System.in);

    public String line(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) {
            System.out.println();
            throw new EndOfInput();
        }
        return in.nextLine().trim();
    }

    public String text(String prompt) {
        while (true) {
            String s = line(prompt);
            if (!s.isEmpty()) {
                return s;
            }
            System.out.println("  This cannot be empty.");
        }
    }

    /** Asks again until the answer matches the pattern. */
    public String matching(String prompt, String regex, String hint) {
        while (true) {
            String s = line(prompt);
            if (s.matches(regex)) {
                return s;
            }
            System.out.println("  " + hint);
        }
    }

    /** Empty answer returns null. */
    public String optionalText(String prompt) {
        String s = line(prompt);
        return s.isEmpty() ? null : s;
    }

    public int number(String prompt, int min, int max) {
        while (true) {
            String s = line(prompt);
            try {
                int n = Integer.parseInt(s);
                if (n >= min && n <= max) {
                    return n;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the message
            }
            System.out.println("  Enter a whole number from " + min + " to " + max + ".");
        }
    }

    public double amount(String prompt) {
        return money(prompt, false);
    }

    /** Like amount, but 0 is allowed (e.g. no security deposit). */
    public double amountOrZero(String prompt) {
        return money(prompt, true);
    }

    private double money(String prompt, boolean allowZero) {
        while (true) {
            String s = line(prompt).replace(",", "").replace("Rs.", "").trim();
            try {
                return Check.money(Double.parseDouble(s), allowZero, "Amount");
            } catch (NumberFormatException e) {
                System.out.println("  Enter an amount in rupees, e.g. 6500.");
            } catch (IllegalArgumentException e) {
                System.out.println("  " + e.getMessage() + ".");
            }
        }
    }

    /** Enter alone gives today. */
    public java.sql.Date date(String prompt) {
        while (true) {
            String s = line(prompt + " [yyyy-mm-dd, Enter = today]: ");
            if (s.isEmpty()) {
                return Dates.today();
            }
            try {
                return Dates.parse(s);
            } catch (ParseException e) {
                System.out.println("  Use the form 2026-11-20.");
            }
        }
    }

    /** Enter alone gives the current month. */
    public String month(String prompt) {
        while (true) {
            String s = line(prompt + " [yyyy-mm, Enter = " + Dates.currentMonth() + "]: ");
            if (s.isEmpty()) {
                return Dates.currentMonth();
            }
            if (s.matches("[0-9]{4}-(0[1-9]|1[0-2])")) {
                return s;
            }
            System.out.println("  Use the form 2026-11.");
        }
    }

    public boolean yesNo(String prompt) {
        while (true) {
            String s = line(prompt + " (y/n): ").toLowerCase();
            if (s.equals("y") || s.equals("yes")) {
                return true;
            }
            if (s.equals("n") || s.equals("no")) {
                return false;
            }
        }
    }

    /** Pick one value of an enum by its number in the list. */
    public <E extends Enum<E>> E choose(String prompt, E[] values) {
        StringBuilder sb = new StringBuilder(prompt).append(" (");
        for (int i = 0; i < values.length; i++) {
            sb.append(i + 1).append('=').append(values[i].name());
            if (i < values.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("): ");
        return values[number(sb.toString(), 1, values.length) - 1];
    }
}
