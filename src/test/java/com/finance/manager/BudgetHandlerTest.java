package com.finance.manager;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BudgetHandlerTest {

    private final BudgetHandler handler = new BudgetHandler();
    private static final LocalDate D = LocalDate.of(2024, 3, 1);

    @Test
    void getRemainingBudget_returnsPositiveWhenUnderBudget() {
        assertEquals(50.0, handler.getRemainingBudget(100.0, 50.0), 0.001);
    }

    @Test
    void getRemainingBudget_returnsNegativeWhenOverBudget() {
        assertTrue(handler.getRemainingBudget(50.0, 100.0) < 0);
    }

    @Test
    void getRemainingBudget_returnsZeroWhenExactlyAtBudget() {
        assertEquals(0.0, handler.getRemainingBudget(75.0, 75.0), 0.001);
    }

    @Test
    void getTotalSpent_emptyListReturnsZero() {
        assertEquals(0.0, handler.getTotalSpent(List.of()), 0.001);
    }

    @Test
    void getTotalSpent_sumsAllExpenses() {
        List<Expense> expenses = List.of(
                new Expense(25.0, "Food", D, ""),
                new Expense(75.0, "Transport", D, "")
        );
        assertEquals(100.0, handler.getTotalSpent(expenses), 0.001);
    }
}
