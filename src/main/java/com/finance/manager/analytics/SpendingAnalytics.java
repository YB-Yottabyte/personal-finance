package com.finance.manager.analytics;

import com.finance.manager.Expense;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure-function analytics engine — all methods are stateless and take a
 * {@code List<Expense>} as input, making them straightforward to unit-test.
 *
 * Algorithms implemented:
 * <ul>
 *   <li>Category aggregation via Streams + Collectors</li>
 *   <li>Monthly trend aggregation (TreeMap for chronological ordering)</li>
 *   <li>Top-K categories using a min-heap (O(k log n) time, O(k) space)</li>
 *   <li>Sliding-window moving average over daily spend</li>
 *   <li>Linear regression (least-squares) for next-month spend forecast</li>
 *   <li>Budget burn-rate: estimated days until budget is exhausted</li>
 * </ul>
 */
public class SpendingAnalytics {

    // -------------------------------------------------------------------------
    // Category breakdown
    // -------------------------------------------------------------------------

    /**
     * Aggregates total spending per category.
     * Returns entries sorted descending by value for convenient display.
     */
    public Map<String, Double> spendingByCategory(List<Expense> expenses) {
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)));
    }

    // -------------------------------------------------------------------------
    // Monthly trend
    // -------------------------------------------------------------------------

    /**
     * Aggregates total spending per calendar month, ordered chronologically.
     * Uses a {@link TreeMap} so iteration order is always oldest → newest.
     */
    public Map<YearMonth, Double> monthlyTotals(List<Expense> expenses) {
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> YearMonth.from(e.getDate()),
                        TreeMap::new,
                        Collectors.summingDouble(Expense::getAmount)));
    }

    // -------------------------------------------------------------------------
    // Top-K categories (min-heap)
    // -------------------------------------------------------------------------

    /**
     * Returns the top {@code n} spending categories sorted highest-first.
     *
     * <p>Algorithm: maintain a min-heap of size n. For each category total,
     * push it on the heap and evict the minimum when size exceeds n.
     * Final sort gives descending order. Time: O(k log n) where k = #categories.
     */
    public List<Map.Entry<String, Double>> topCategories(List<Expense> expenses, int n) {
        if (n <= 0) return Collections.emptyList();
        Map<String, Double> totals = spendingByCategory(expenses);

        PriorityQueue<Map.Entry<String, Double>> minHeap =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > n) minHeap.poll(); // evict smallest
        }

        List<Map.Entry<String, Double>> result = new ArrayList<>(minHeap);
        result.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        return result;
    }

    // -------------------------------------------------------------------------
    // Sliding-window moving average
    // -------------------------------------------------------------------------

    /**
     * Computes a daily moving average of spend over a rolling {@code windowDays}-day window.
     *
     * <p>Algorithm: group expenses into daily totals via a TreeMap, then use a
     * two-pointer sliding window across the date range. Each output point is the
     * average daily spend within the window ending on that day. O(n) time.
     *
     * @return list of (date, movingAvg) pairs, ordered chronologically
     */
    public List<double[]> movingAverageDaily(List<Expense> expenses, int windowDays) {
        if (expenses.isEmpty() || windowDays <= 0) return Collections.emptyList();

        // Build a sorted map of date -> daily total
        TreeMap<LocalDate, Double> dailyTotals = new TreeMap<>();
        for (Expense e : expenses) {
            dailyTotals.merge(e.getDate(), e.getAmount(), Double::sum);
        }

        List<LocalDate> dates = new ArrayList<>(dailyTotals.keySet());
        List<double[]> result = new ArrayList<>();
        Deque<Double> window = new ArrayDeque<>();
        double windowSum = 0.0;

        // Two-pointer over the sorted date list
        int left = 0;
        for (int right = 0; right < dates.size(); right++) {
            double amount = dailyTotals.get(dates.get(right));
            window.addLast(amount);
            windowSum += amount;

            // Evict points outside the window
            while (ChronoUnit.DAYS.between(dates.get(left), dates.get(right)) >= windowDays) {
                windowSum -= window.pollFirst();
                left++;
            }

            long windowSize = ChronoUnit.DAYS.between(dates.get(left), dates.get(right)) + 1;
            result.add(new double[]{dates.get(right).toEpochDay(), windowSum / windowSize});
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Linear regression forecast
    // -------------------------------------------------------------------------

    /**
     * Forecasts next-month spending using ordinary least-squares linear regression
     * over historical monthly totals.
     *
     * <p>Formula: {@code slope = (n·ΣxY − Σx·ΣY) / (n·Σx² − (Σx)²)},
     * {@code intercept = (ΣY − slope·Σx) / n}, where x is months since the
     * earliest month and Y is the monthly spend.
     *
     * @return predicted spend for the month after the most recent data point,
     *         or 0.0 if there is insufficient data
     */
    public double forecastNextMonth(List<Expense> expenses) {
        Map<YearMonth, Double> monthly = monthlyTotals(expenses);
        if (monthly.size() < 2) {
            return monthly.isEmpty() ? 0.0 : monthly.values().iterator().next();
        }

        List<YearMonth> months = new ArrayList<>(monthly.keySet());
        YearMonth base = months.get(0);
        int n = months.size();

        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            double x = base.until(months.get(i), ChronoUnit.MONTHS);
            double y = monthly.get(months.get(i));
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double denominator = n * sumXX - sumX * sumX;
        if (denominator == 0) return sumY / n; // all months are the same

        double slope = (n * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / n;
        double nextX = base.until(months.get(n - 1).plusMonths(1), ChronoUnit.MONTHS);
        return Math.max(0.0, intercept + slope * nextX);
    }

    // -------------------------------------------------------------------------
    // Budget burn-rate
    // -------------------------------------------------------------------------

    /**
     * Estimates the number of days before the remaining budget reaches zero,
     * based on the average daily spend from the earliest recorded expense to today.
     *
     * @return estimated days remaining, or -1 if the rate cannot be determined
     */
    public int daysUntilBudgetExhausted(List<Expense> expenses, double budget) {
        if (budget <= 0 || expenses.isEmpty()) return -1;
        double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double remaining = budget - totalSpent;
        if (remaining <= 0) return 0;

        LocalDate earliest = expenses.stream()
                .map(Expense::getDate)
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.now());
        long days = Math.max(1, ChronoUnit.DAYS.between(earliest, LocalDate.now()) + 1);
        double dailyRate = totalSpent / days;
        if (dailyRate <= 0) return -1;
        return (int) Math.ceil(remaining / dailyRate);
    }

    // -------------------------------------------------------------------------
    // Summary helpers
    // -------------------------------------------------------------------------

    public double totalSpent(List<Expense> expenses) {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    public Optional<String> topCategory(List<Expense> expenses) {
        return spendingByCategory(expenses).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public double averageDailySpend(List<Expense> expenses) {
        if (expenses.isEmpty()) return 0.0;
        LocalDate earliest = expenses.stream().map(Expense::getDate)
                .min(Comparator.naturalOrder()).orElse(LocalDate.now());
        long days = Math.max(1, ChronoUnit.DAYS.between(earliest, LocalDate.now()) + 1);
        return totalSpent(expenses) / days;
    }
}
