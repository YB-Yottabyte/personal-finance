package com.finance.manager;

import java.time.LocalDate;

public class Expense {
    private double amount;
    private String category;
    private LocalDate date;
    private String description;

    public Expense(double amount, String category, LocalDate date, String description) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("Expense{amount=%.2f, category='%s', date=%s, description='%s'}",
                amount, category, date, description);
    }

    public String toCSV() {
        return String.format("%.2f,%s,%s,%s", amount, category, date, description);
    }

    public static Expense fromCSV(String csvLine) {
        String[] fields = csvLine.split(",");
        double amount = Double.parseDouble(fields[0]);
        String category = fields[1];
        LocalDate date = LocalDate.parse(fields[2]);
        String description = fields[3];
        return new Expense(amount, category, date, description);
    }
}
