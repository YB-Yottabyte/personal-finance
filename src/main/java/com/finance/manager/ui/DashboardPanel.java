package com.finance.manager.ui;

import com.finance.manager.BudgetConfig;
import com.finance.manager.Expense;
import com.finance.manager.analytics.SpendingAnalytics;
import com.finance.manager.analytics.SpendingAnomaly;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dashboard tab: pie chart of spending by category + live summary statistics.
 */
public class DashboardPanel extends JPanel {

    private final SpendingAnalytics analytics = new SpendingAnalytics();
    private final SpendingAnomaly anomalyDetector = new SpendingAnomaly();

    private final DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();
    private final JLabel totalLabel    = new JLabel();
    private final JLabel budgetLabel   = new JLabel();
    private final JLabel remainLabel   = new JLabel();
    private final JLabel topCatLabel   = new JLabel();
    private final JLabel avgDayLabel   = new JLabel();
    private final JLabel burnLabel     = new JLabel();
    private final JLabel anomalyLabel  = new JLabel();
    private final JLabel forecastLabel = new JLabel();
    private final JProgressBar budgetBar = new JProgressBar(0, 100);

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---- Pie chart (left) -----------------------------------------------
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Spending by Category", pieDataset, true, true, false);
        PiePlot<?> plot = (PiePlot<?>) pieChart.getPlot();
        plot.setBackgroundPaint(UIManager.getColor("Panel.background"));
        plot.setOutlineVisible(false);
        pieChart.setBackgroundPaint(UIManager.getColor("Panel.background"));

        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new Dimension(400, 320));
        chartPanel.setMouseWheelEnabled(true);
        add(chartPanel, BorderLayout.CENTER);

        // ---- Stats panel (right) --------------------------------------------
        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        stats.setBorder(BorderFactory.createTitledBorder("Summary"));
        stats.setPreferredSize(new Dimension(230, 320));

        budgetBar.setStringPainted(true);
        budgetBar.setString("Budget used");

        stats.add(makeStat("Total Spent", totalLabel));
        stats.add(makeStat("Budget", budgetLabel));
        stats.add(makeStat("Remaining", remainLabel));
        stats.add(Box.createVerticalStrut(6));
        stats.add(budgetBar);
        stats.add(Box.createVerticalStrut(10));
        stats.add(makeStat("Top Category", topCatLabel));
        stats.add(makeStat("Avg/Day", avgDayLabel));
        stats.add(makeStat("Days Left in Budget", burnLabel));
        stats.add(makeStat("Next Month Forecast", forecastLabel));
        stats.add(Box.createVerticalStrut(10));
        stats.add(makeStat("Anomalies Detected", anomalyLabel));

        add(stats, BorderLayout.EAST);
    }

    /** Call this after any expense or budget change to sync all widgets. */
    public void refresh(List<Expense> expenses, BudgetConfig budget) {
        // Update pie dataset
        pieDataset.clear();
        Map<String, Double> byCategory = analytics.spendingByCategory(expenses);
        byCategory.forEach(pieDataset::setValue);

        // Update stats
        double total    = analytics.totalSpent(expenses);
        double remain   = budget.amount() - total;
        Optional<String> topCat = analytics.topCategory(expenses);
        double avgDay   = analytics.averageDailySpend(expenses);
        int burnDays    = analytics.daysUntilBudgetExhausted(expenses, budget.amount());
        double forecast = analytics.forecastNextMonth(expenses);
        int anomalies   = anomalyDetector.detectAnomalies(expenses).size();

        totalLabel.setText(String.format("$%.2f", total));
        budgetLabel.setText(budget.isSet()
                ? String.format("$%.2f / %s", budget.amount(), budget.displayPeriod())
                : "Not set");
        remainLabel.setText(String.format("$%.2f", remain));
        remainLabel.setForeground(remain < 0 ? Color.RED : new Color(0, 130, 0));

        if (budget.amount() > 0) {
            int pct = (int) Math.min(100, (total / budget.amount()) * 100);
            budgetBar.setValue(pct);
            budgetBar.setString(pct + "% used");
            budgetBar.setForeground(pct >= 90 ? Color.RED : pct >= 70 ? Color.ORANGE : new Color(0, 150, 0));
            budgetBar.setVisible(true);
        } else {
            budgetBar.setVisible(false);
        }

        topCatLabel.setText(topCat.orElse("—"));
        avgDayLabel.setText(String.format("$%.2f", avgDay));
        burnLabel.setText(burnDays < 0 ? "—" : burnDays + " days");
        forecastLabel.setText(String.format("$%.2f", forecast));
        anomalyLabel.setText(anomalies == 0 ? "None" : String.valueOf(anomalies));
        anomalyLabel.setForeground(anomalies > 0 ? Color.RED : Color.BLACK);

        revalidate();
        repaint();
    }

    private JPanel makeStat(String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel lbl = new JLabel(label + ": ");
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 12f));
        value.setFont(value.getFont().deriveFont(Font.BOLD, 12f));
        row.add(lbl, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }
}
