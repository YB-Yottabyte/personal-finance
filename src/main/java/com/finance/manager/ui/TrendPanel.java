package com.finance.manager.ui;

import com.finance.manager.Expense;
import com.finance.manager.analytics.SpendingAnalytics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Trends tab: bar chart of monthly spending with a linear-regression
 * forecast annotation for the next month.
 */
public class TrendPanel extends JPanel {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM yyyy");

    private final SpendingAnalytics analytics = new SpendingAnalytics();
    private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    private final JLabel forecastLabel = new JLabel("—");
    private final JLabel insightLabel  = new JLabel("Add expenses to see trends.");

    public TrendPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Bar chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Monthly Spending", "Month", "Amount ($)",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(UIManager.getColor("Panel.background"));
        plot.setOutlineVisible(false);
        chart.setBackgroundPaint(UIManager.getColor("Panel.background"));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189));
        renderer.setMaximumBarWidth(0.15);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        add(chartPanel, BorderLayout.CENTER);

        // Forecast footer
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 4));
        footer.setBorder(new EmptyBorder(6, 4, 4, 4));

        JPanel forecastRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        forecastRow.add(new JLabel("Next-Month Forecast (linear regression):"));
        forecastLabel.setFont(forecastLabel.getFont().deriveFont(Font.BOLD, 14f));
        forecastLabel.setForeground(new Color(79, 129, 189));
        forecastRow.add(forecastLabel);
        footer.add(forecastRow);

        insightLabel.setFont(insightLabel.getFont().deriveFont(Font.ITALIC, 12f));
        insightLabel.setForeground(Color.GRAY);
        footer.add(insightLabel);

        add(footer, BorderLayout.SOUTH);
    }

    /** Rebuilds the chart and forecast from the current expense list. */
    public void refresh(List<Expense> expenses) {
        dataset.clear();
        Map<YearMonth, Double> monthly = analytics.monthlyTotals(expenses);
        monthly.forEach((month, total) ->
                dataset.addValue(total, "Spending", month.format(MONTH_FMT)));

        double forecast = analytics.forecastNextMonth(expenses);
        forecastLabel.setText(expenses.size() < 2 ? "—" : String.format("$%.2f", forecast));

        // Simple human-readable insight
        if (monthly.size() >= 2) {
            List<Double> values = List.copyOf(monthly.values());
            double last  = values.get(values.size() - 1);
            double prev  = values.get(values.size() - 2);
            double delta = last - prev;
            String trend = delta > 0 ? String.format("+$%.2f vs prior month", delta)
                                     : String.format("-$%.2f vs prior month", Math.abs(delta));
            insightLabel.setText("Last month: " + trend);
        } else {
            insightLabel.setText("Add more expenses across months to see a trend.");
        }

        revalidate();
        repaint();
    }
}
