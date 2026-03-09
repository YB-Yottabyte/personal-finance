package com.finance.manager;

import com.finance.manager.service.BudgetService;
import com.finance.manager.service.ExpenseService;
import com.finance.manager.ui.DashboardPanel;
import com.finance.manager.ui.ExpenseTablePanel;
import com.finance.manager.ui.TrendPanel;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Swing UI root — wired by Spring as a singleton {@link Component}.
 * {@link #show()} is called from {@link FinanceManagerApplication} on the EDT
 * after the Spring context finishes starting.
 */
@Component
public class MainApp {

    private final ExpenseService expenseService;
    private final BudgetService  budgetService;

    private BudgetConfig budgetConfig;
    private boolean      darkMode = false;
    private JFrame       frame;

    public MainApp(ExpenseService expenseService, BudgetService budgetService) {
        this.expenseService = expenseService;
        this.budgetService  = budgetService;
    }

    /** Entry point called from the EDT after Spring Boot starts. */
    public void show() {
        FlatLightLaf.setup();
        budgetConfig = budgetService.load();
        buildFrame();
    }

    // ── Frame assembly ────────────────────────────────────────────────────────

    private void buildFrame() {
        frame = new JFrame("Personal Finance Manager");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(900, 580);
        frame.setMinimumSize(new Dimension(700, 480));
        frame.setLayout(new BorderLayout());

        DashboardPanel      dash         = new DashboardPanel();
        TrendPanel          trend        = new TrendPanel();
        // Array holder lets the lambda capture expTab before assignment completes
        ExpenseTablePanel[] expTabHolder = new ExpenseTablePanel[1];
        expTabHolder[0] = new ExpenseTablePanel(
                expenseService,
                () -> refreshAll(dash, trend, expTabHolder[0]));
        ExpenseTablePanel expTab = expTabHolder[0];

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dash);
        tabs.addTab("Expenses",  expTab);
        tabs.addTab("Trends",    trend);
        frame.add(tabs, BorderLayout.CENTER);
        frame.add(buildTopBar(), BorderLayout.NORTH);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                onClose();
            }
        });

        refreshAll(dash, trend, expTab);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel bar   = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JButton budgetBtn = new JButton("Set Budget");
        JButton darkBtn   = new JButton("\u2600");           // ☀
        darkBtn.setFont(darkBtn.getFont().deriveFont(16f));
        darkBtn.setFocusPainted(false);
        darkBtn.setBorderPainted(false);
        darkBtn.setContentAreaFilled(false);
        darkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        budgetBtn.addActionListener(e -> {
            showBudgetDialog();
            // panels will refresh via their own callback after dialog closes
        });
        darkBtn.addActionListener(e -> {
            darkMode = !darkMode;
            if (darkMode) FlatDarkLaf.setup(); else FlatLightLaf.setup();
            SwingUtilities.updateComponentTreeUI(frame);
            frame.revalidate();
            frame.repaint();
            darkBtn.setText(darkMode ? "\ud83c\udf19" : "\u2600");  // 🌙 / ☀
        });

        right.add(budgetBtn);
        right.add(darkBtn);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    private void refreshAll(DashboardPanel dash, TrendPanel trend, ExpenseTablePanel expTab) {
        List<Expense> expenses = expenseService.getAllExpenses();
        dash.refresh(expenses, budgetConfig);
        trend.refresh(expenses);
        expTab.refresh(expenses);
    }

    // ── Budget dialog ─────────────────────────────────────────────────────────

    private void showBudgetDialog() {
        JTextField     amountField = new JTextField(10);
        JComboBox<String> combo    = new JComboBox<>(new String[]{"monthly", "weekly"});

        if (budgetConfig.isSet()) {
            amountField.setText(String.format("%.2f", budgetConfig.amount()));
            combo.setSelectedItem(budgetConfig.period());
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
        panel.add(new JLabel("Budget Amount ($):")); panel.add(amountField);
        panel.add(new JLabel("Period:"));            panel.add(combo);

        int opt = JOptionPane.showConfirmDialog(frame, panel, "Set Budget",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) return;

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount < 0) { warn("Budget cannot be negative."); return; }
            budgetConfig = new BudgetConfig(amount, (String) combo.getSelectedItem());
            budgetService.save(budgetConfig);
        } catch (NumberFormatException ex) {
            warn("Invalid amount.");
        }
    }

    // ── Window close ──────────────────────────────────────────────────────────

    private void onClose() {
        int choice = JOptionPane.showConfirmDialog(frame,
                "Export expenses before closing?", "Export",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (choice == JOptionPane.CANCEL_OPTION) return;
        if (choice == JOptionPane.YES_OPTION) {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("expenses.csv"));
            if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                String path = fc.getSelectedFile().getAbsolutePath();
                if (!path.endsWith(".csv")) path += ".csv";
                expenseService.exportToCSV(path);
            }
        }
        System.exit(0);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}
