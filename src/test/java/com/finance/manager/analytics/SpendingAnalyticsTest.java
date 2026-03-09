package com.finance.manager.analytics;

import com.finance.manager.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpendingAnalyticsTest {

    private SpendingAnalytics analytics;
    private static final LocalDate JAN = LocalDate.of(2024, 1, 15);
    private static final LocalDate FEB = LocalDate.of(2024, 2, 10);
    private static final LocalDate MAR = LocalDate.of(2024, 3, 5);

    @BeforeEach
    void setUp() {
        analytics = new SpendingAnalytics();
    }

    // ---- Category aggregation -----------------------------------------------

    @Test
    void spendingByCategory_sumsCorrectly() {
        List<Expense> expenses = List.of(
                new Expense(10.0, "Food", JAN, "lunch"),
                new Expense(20.0, "Food", JAN, "dinner"),
                new Expense(50.0, "Transport", JAN, "train")
        );
        Map<String, Double> result = analytics.spendingByCategory(expenses);
        assertEquals(30.0, result.get("Food"), 0.001);
        assertEquals(50.0, result.get("Transport"), 0.001);
    }

    @Test
    void spendingByCategory_emptyListReturnsEmptyMap() {
        assertTrue(analytics.spendingByCategory(List.of()).isEmpty());
    }

    // ---- Monthly totals -----------------------------------------------------

    @Test
    void monthlyTotals_groupsByMonth() {
        List<Expense> expenses = List.of(
                new Expense(100.0, "Food", JAN, ""),
                new Expense(200.0, "Food", FEB, ""),
                new Expense(50.0,  "Food", FEB, "")
        );
        Map<?, Double> result = analytics.monthlyTotals(expenses);
        assertEquals(2, result.size());
        assertEquals(350.0, result.values().stream().mapToDouble(Double::doubleValue).sum(), 0.001);
    }

    // ---- Top-K categories (parameterized) -----------------------------------

    @ParameterizedTest(name = "top {0} categories from 3")
    @CsvSource({"1,Transport", "2,Transport", "3,Transport"})
    void topCategories_returnsCorrectTopN(int n, String expectedFirst) {
        List<Expense> expenses = List.of(
                new Expense(10.0, "Food",      JAN, ""),
                new Expense(80.0, "Transport", JAN, ""),
                new Expense(40.0, "Housing",   JAN, "")
        );
        var result = analytics.topCategories(expenses, n);
        assertEquals(Math.min(n, 3), result.size());
        assertEquals(expectedFirst, result.get(0).getKey());
    }

    @Test
    void topCategories_nZeroReturnsEmpty() {
        assertTrue(analytics.topCategories(List.of(new Expense(10, "Food", JAN, "")), 0).isEmpty());
    }

    @Test
    void topCategories_nLargerThanCategoriesReturnsAll() {
        List<Expense> expenses = List.of(
                new Expense(5.0, "Food", JAN, ""),
                new Expense(3.0, "Transport", JAN, "")
        );
        assertEquals(2, analytics.topCategories(expenses, 100).size());
    }

    // ---- Forecast (linear regression) ---------------------------------------

    @Test
    void forecastNextMonth_singleMonthReturnsThatMonth() {
        List<Expense> expenses = List.of(new Expense(200.0, "Food", JAN, ""));
        assertEquals(200.0, analytics.forecastNextMonth(expenses), 0.001);
    }

    @Test
    void forecastNextMonth_upwardTrendForecastsHigher() {
        // Jan=$100, Feb=$200, Mar=$300 — perfect upward trend; forecast Apr ≈ $400
        List<Expense> expenses = List.of(
                new Expense(100.0, "Food", JAN, ""),
                new Expense(200.0, "Food", FEB, ""),
                new Expense(300.0, "Food", MAR, "")
        );
        double forecast = analytics.forecastNextMonth(expenses);
        assertTrue(forecast > 300.0, "Expected forecast > 300 but was " + forecast);
    }

    @Test
    void forecastNextMonth_emptyListReturnsZero() {
        assertEquals(0.0, analytics.forecastNextMonth(List.of()), 0.001);
    }

    // ---- Budget burn rate ---------------------------------------------------

    @Test
    void daysUntilBudgetExhausted_returnsNegativeOneWhenNoBudget() {
        assertEquals(-1, analytics.daysUntilBudgetExhausted(List.of(), 0));
    }

    @Test
    void daysUntilBudgetExhausted_returnsZeroWhenOverBudget() {
        // budget=$50 but spent=$100
        List<Expense> expenses = List.of(new Expense(100.0, "Food", LocalDate.now().minusDays(5), ""));
        assertEquals(0, analytics.daysUntilBudgetExhausted(expenses, 50.0));
    }

    // ---- Total and average --------------------------------------------------

    @Test
    void totalSpent_sumsAllAmounts() {
        List<Expense> expenses = List.of(
                new Expense(15.0, "Food", JAN, ""),
                new Expense(35.0, "Food", JAN, "")
        );
        assertEquals(50.0, analytics.totalSpent(expenses), 0.001);
    }

    @Test
    void topCategory_returnsHighestSpendingCategory() {
        List<Expense> expenses = List.of(
                new Expense(10.0, "Food",      JAN, ""),
                new Expense(99.0, "Transport", JAN, "")
        );
        assertEquals("Transport", analytics.topCategory(expenses).orElse("none"));
    }

    @Test
    void topCategory_emptyListReturnsEmpty() {
        assertTrue(analytics.topCategory(List.of()).isEmpty());
    }
}
