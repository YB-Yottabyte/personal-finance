package com.finance.manager;

import java.time.LocalDate;

public class Expense {
    private final double amount;
    private final String category;
    private final LocalDate date;
    private final String description;

    public Expense(double amount, String category, LocalDate date, String description) {
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative.");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Category cannot be empty.");
        if (date == null) throw new IllegalArgumentException("Date cannot be null.");
        this.amount = amount;
        this.category = category.trim();
        this.date = date;
        this.description = (description == null) ? "" : description.trim();
    }

    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("Expense{amount=%.2f, category='%s', date=%s, description='%s'}",
                amount, category, date, description);
    }

    /**
     * Serializes to CSV using RFC 4180 quoting: fields containing commas or quotes
     * are wrapped in double-quotes, and any internal double-quotes are escaped as "".
     */
    public String toCSV() {
        return String.format("%.2f,%s,%s,%s",
                amount,
                csvEscape(category),
                date,
                csvEscape(description));
    }

    /**
     * Parses a CSV line produced by {@link #toCSV()}.
     * Handles quoted fields so descriptions with commas round-trip correctly.
     */
    public static Expense fromCSV(String csvLine) {
        String[] fields = parseCsvLine(csvLine);
        if (fields.length < 4) {
            throw new IllegalArgumentException("Invalid CSV line: " + csvLine);
        }
        double amount = Double.parseDouble(fields[0].trim());
        String category = fields[1].trim();
        LocalDate date = LocalDate.parse(fields[2].trim());
        String description = fields[3].trim();
        return new Expense(amount, category, date, description);
    }

    private static String csvEscape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** Minimal RFC 4180 CSV parser for a single line. */
    static String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // skip escaped quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
