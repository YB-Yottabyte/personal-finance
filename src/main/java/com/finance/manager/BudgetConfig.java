package com.finance.manager;

/**
 * Immutable value object representing a user's budget configuration.
 * Using a Java record eliminates boilerplate and signals modern Java (17+) fluency.
 */
public record BudgetConfig(double amount, String period) {

    public static final BudgetConfig UNSET = new BudgetConfig(0.0, "");

    /** Compact constructor for validation. */
    public BudgetConfig {
        if (amount < 0) throw new IllegalArgumentException("Budget amount cannot be negative.");
        period = (period == null) ? "" : period.trim();
    }

    public boolean isSet() {
        return amount > 0 && !period.isBlank();
    }

    public String displayPeriod() {
        return period.isBlank() ? "Not set" : period;
    }
}
