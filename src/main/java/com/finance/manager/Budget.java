package com.finance.manager;

import jakarta.persistence.*;

/**
 * JPA entity for the user's budget configuration.
 * Only one row ever exists (id is always 1) — saved/updated with a simple
 * {@code save()} call thanks to Spring Data's upsert-by-id behaviour.
 */
@Entity
@Table(name = "budget")
public class Budget {

    @Id
    private Long id = 1L;   // singleton row

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String period;

    protected Budget() {}

    public Budget(Double amount, String period) {
        this.id     = 1L;
        this.amount = amount;
        this.period = period;
    }

    public Long   getId()     { return id; }
    public Double getAmount() { return amount; }
    public String getPeriod() { return period; }

    /** Converts to the lightweight {@link BudgetConfig} record used by the UI. */
    public BudgetConfig toConfig() {
        return new BudgetConfig(amount, period);
    }

    /** Factory: builds a {@link Budget} entity from a {@link BudgetConfig} record. */
    public static Budget from(BudgetConfig config) {
        return new Budget(config.amount(), config.period());
    }
}
