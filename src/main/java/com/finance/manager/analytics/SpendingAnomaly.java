package com.finance.manager.analytics;

import com.finance.manager.Expense;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Detects anomalous expenses using a per-category z-score analysis.
 *
 * <p>An expense is flagged as anomalous when its amount deviates more than
 * {@code Z_SCORE_THRESHOLD} standard deviations from the mean for its category.
 * Categories with fewer than 3 samples are excluded (insufficient data).
 *
 * <p>Standard deviation is computed in two O(n) passes (mean, then variance)
 * to avoid the numerical instability of the single-pass formula.
 */
public class SpendingAnomaly {

    public static final double Z_SCORE_THRESHOLD = 2.0;
    private static final int MIN_SAMPLE_SIZE = 3;

    /** Immutable result for a single detected anomaly. */
    public record AnomalyResult(Expense expense, double zScore) {
        /** True when the expense is unusually high (positive z-score). */
        public boolean isHighSpend() { return zScore > 0; }
    }

    /**
     * Returns all expenses whose z-score (within their category) exceeds
     * {@link #Z_SCORE_THRESHOLD}, sorted by absolute z-score descending.
     */
    public List<AnomalyResult> detectAnomalies(List<Expense> expenses) {
        // Group amounts by category
        Map<String, List<Double>> amountsByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.mapping(Expense::getAmount, Collectors.toList())));

        List<AnomalyResult> anomalies = new ArrayList<>();

        for (Expense expense : expenses) {
            List<Double> amounts = amountsByCategory.get(expense.getCategory());
            if (amounts == null || amounts.size() < MIN_SAMPLE_SIZE) continue;

            double mean = amounts.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = amounts.stream()
                    .mapToDouble(a -> (a - mean) * (a - mean))
                    .average()
                    .orElse(0);
            double stddev = Math.sqrt(variance);
            if (stddev == 0) continue;

            double zScore = (expense.getAmount() - mean) / stddev;
            if (Math.abs(zScore) > Z_SCORE_THRESHOLD) {
                anomalies.add(new AnomalyResult(expense, zScore));
            }
        }

        anomalies.sort(Comparator.comparingDouble(r -> -Math.abs(r.zScore())));
        return anomalies;
    }
}
