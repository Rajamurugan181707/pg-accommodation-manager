package com.pgms.util;

import com.pgms.ds.DynamicArray;

/** Prints rows as a neat text table that fits a normal terminal. */
public class Table {

    private final String[] headers;
    private final DynamicArray<String[]> rows = new DynamicArray<String[]>();

    public Table(String... headers) {
        this.headers = headers;
    }

    public void addRow(Object... cells) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = i < cells.length && cells[i] != null ? String.valueOf(cells[i]) : "-";
        }
        rows.add(row);
    }

    public void print() {
        int[] width = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            width[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                width[i] = Math.max(width[i], row[i].length());
            }
        }
        String line = separator(width);
        System.out.println(line);
        System.out.println(format(headers, width));
        System.out.println(line);
        for (String[] row : rows) {
            System.out.println(format(row, width));
        }
        System.out.println(line);
        if (rows.isEmpty()) {
            System.out.println("  (nothing to show)");
        }
    }

    private static String format(String[] cells, int[] width) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < cells.length; i++) {
            sb.append(' ').append(pad(cells[i], width[i])).append(" |");
        }
        return sb.toString();
    }

    private static String separator(int[] width) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : width) {
            for (int i = 0; i < w + 2; i++) {
                sb.append('-');
            }
            sb.append('+');
        }
        return sb.toString();
    }

    private static String pad(String s, int w) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < w) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
