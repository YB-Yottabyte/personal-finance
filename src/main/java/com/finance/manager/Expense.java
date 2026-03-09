package com.finance.manager;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * JPA entity representing a single expense.
 *
 * <p>Hibernate manages persistence via Spring Data JPA — no manual JDBC needed.
 * The CSV serialisation methods are kept so import/export still works.
 */
@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_date",     columnList = "date"),
        @Index(name = "idx_expense_category", columnList = "category")
})
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String description = "";

    /** Required by JPA — do not use directly. */
    protected Expense() {}

    /** Creates a new expense (id assigned by the database on save). */
    public Expense(double amount, String category, LocalDate date, String description) {
        validate(amount, category, date);
        this.amount      = amount;
        this.category    = category.trim();
        this.date        = date;
        this.description = description == null ? "" : description.trim();
    }

    // ---- Getters ------------------------------------------------------------

    public Long      getId()          { return id; }
    public double    getAmount()      { return amount; }
    public String    getCategory()    { return category; }
    public LocalDate getDate()        { return date; }
    public String    getDescription() { return description; }

    // ---- CSV ----------------------------------------------------------------

    /**
     * Serialises to RFC 4180 CSV: fields with commas/quotes are double-quoted,
     * internal quotes are escaped as {@code ""}.
     */
    public String toCSV() {
        return String.format("%.2f,%s,%s,%s",
                amount, csvEscape(category), date, csvEscape(description));
    }

    /** Parses a CSV line produced by {@link #toCSV()}. */
    public static Expense fromCSV(String csvLine) {
        String[] f = parseCsvLine(csvLine);
        if (f.length < 4) throw new IllegalArgumentException("Invalid CSV line: " + csvLine);
        return new Expense(
                Double.parseDouble(f[0].trim()),
                f[1].trim(),
                LocalDate.parse(f[2].trim()),
                f[3].trim());
    }

    // ---- Helpers ------------------------------------------------------------

    private static void validate(double amount, String category, LocalDate date) {
        if (amount < 0)                             throw new IllegalArgumentException("Amount cannot be negative.");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Category cannot be empty.");
        if (date == null)                           throw new IllegalArgumentException("Date cannot be null.");
    }

    private static String csvEscape(String v) {
        if (v.contains(",") || v.contains("\"") || v.contains("\n"))
            return "\"" + v.replace("\"", "\"\"") + "\"";
        return v;
    }

    static String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                    else inQuotes = false;
                } else cur.append(c);
            } else {
                if      (c == '"') inQuotes = true;
                else if (c == ',') { fields.add(cur.toString()); cur.setLength(0); }
                else               cur.append(c);
            }
        }
        fields.add(cur.toString());
        return fields.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return String.format("Expense{id=%d, amount=%.2f, category='%s', date=%s}",
                id, amount, category, date);
    }
}
