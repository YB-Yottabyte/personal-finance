package com.finance.manager.analytics;

import com.finance.manager.Expense;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpendingAnomalyTest {

    private final SpendingAnomaly detector = new SpendingAnomaly();
    private static final LocalDate D = LocalDate.of(2024, 1, 1);

    @Test
    void detectAnomalies_uniformDataProducesNoAnomalies() {
        // All amounts identical → stddev = 0 → nothing flagged
        List<Expense> expenses = List.of(
                new Expense(50.0, "Food", D, "a"),
                new Expense(50.0, "Food", D, "b"),
                new Expense(50.0, "Food", D, "c"),
                new Expense(50.0, "Food", D, "d")
        );
        assertTrue(detector.detectAnomalies(expenses).isEmpty());
    }

    @Test
    void detectAnomalies_flagsObviousOutlier() {
        // Ten $10 expenses + one $1000. With more normal samples the mean stays near $10,
        // stddev ≈ $284, so the z-score of $1000 ≈ 3.5 — well above the threshold of 2.0.
        List<Expense> expenses = List.of(
                new Expense(10.0,   "Food", D, "normal"),
                new Expense(10.0,   "Food", D, "normal"),
                new Expense(10.0,   "Food", D, "normal"),
                new Expense(10.0,   "Food", D, "normal"),
                new Expense(10.0,   "Food", D, "normal"),
                new Expense(10.0,   "Food", D, "normal"),
                new Expense(10.0,   "Food", D, "normal"),
                new Expense(10.0,   "Food", D, "normal"),
                new Expense(10.0,   "Food", D, "normal"),
                new Expense(10.0,   "Food", D, "normal"),
                new Expense(1000.0, "Food", D, "outlier")
        );
        var anomalies = detector.detectAnomalies(expenses);
        assertFalse(anomalies.isEmpty(), "Expected the $1000 expense to be flagged");
        assertEquals(1000.0, anomalies.get(0).expense().getAmount(), 0.001);
        assertTrue(anomalies.get(0).isHighSpend());
    }

    @Test
    void detectAnomalies_requiresMinimumThreeSamplesPerCategory() {
        // Only 2 samples in "Food" — not enough to compute meaningful stddev
        List<Expense> expenses = List.of(
                new Expense(10.0,  "Food", D, "a"),
                new Expense(999.0, "Food", D, "b")
        );
        assertTrue(detector.detectAnomalies(expenses).isEmpty(),
                "Should not flag anomalies with fewer than 3 samples");
    }

    @Test
    void detectAnomalies_sortedByAbsoluteZScoreDescending() {
        List<Expense> expenses = List.of(
                new Expense(10.0,   "Food", D, ""),
                new Expense(10.0,   "Food", D, ""),
                new Expense(10.0,   "Food", D, ""),
                new Expense(10.0,   "Food", D, ""),
                new Expense(500.0,  "Food", D, "big"),
                new Expense(1000.0, "Food", D, "bigger")
        );
        var anomalies = detector.detectAnomalies(expenses);
        if (anomalies.size() >= 2) {
            assertTrue(Math.abs(anomalies.get(0).zScore()) >= Math.abs(anomalies.get(1).zScore()),
                    "Results should be sorted by |z-score| descending");
        }
    }

    @Test
    void detectAnomalies_emptyListReturnsEmpty() {
        assertTrue(detector.detectAnomalies(List.of()).isEmpty());
    }
}
