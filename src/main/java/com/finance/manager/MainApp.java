package com.finance.manager;

import com.finance.manager.ui.DashboardPanel;
import com.finance.manager.ui.ExpenseTablePanel;
import com.finance.manager.ui.TrendPanel;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Application entry point. Responsibilities:
 * <ul>
 *   <li>Bootstrap the Look and Feel (FlatLaf)</li>
 *   <li>Assemble the three UI panels into a {@link JTabbedPane}</li>
 *   <li>Wire up the shared {@link ExpenseTrackerService} and route data-change
 *       events from any panel back to all panels (Observer pattern)</li>
 * </ul>
 * Business logic lives in the service/handler layer; rendering lives in the panels.
 */
public class MainApp {

    private final ExpenseTrackerService service = new ExpenseTrackerService();
    private BudgetConfig budgetConfig;
    private boolean darkMode = false;

    public static void main(String[] args) {
        FlatLightLaf.setup(); // modern flat UI — replaces the gray Swing default
        SwingUtilities.invokeLater(new MainApp()::createUI);
    }

    private void createUI() {
        budgetConfig = service.loadBudget();

        JFrame frame = new JFrame("Personal Finance Manager");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(900, 580);
        frame.setMinimumSize(new Dimension(700, 480));
        frame.setLayout(new BorderLayout());

        // ---- Panels ---------------------------------------------------------
        DashboardPanel dashboardPanel  = new DashboardPanel();
        TrendPanel trendPanel          = new TrendPanel();
        // Lambda captures panel references before they're fully assigned — use array trick
        ExpenseTablePanel[] epHolder   = new ExpenseTablePanel[1];
        DashboardPanel[]    dpHolder   = new DashboardPanel[]{dashboardPanel};
        TrendPanel[]        tpHolder   = new TrendPanel[]{trendPanel};

        epHolder[0] = new ExpenseTablePanel(service,
                () -> refreshAllPanels(dpHolder[0], tpHolder[0], epHolder[0]));

        // ---- Tabs -----------------------------------------------------------
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboardPanel);
        tabs.addTab("Expenses",  epHolder[0]);
        tabs.addTab("Trends",    trendPanel);
        frame.add(tabs, BorderLayout.CENTER);

        // ---- Top bar: budget button + dark mode toggle ----------------------
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JButton setBudgetBtn  = new JButton("Set Budget");
        JToggleButton darkBtn = new JToggleButton("Dark Mode");
        darkBtn.addActionListener(e -> toggleDarkMode(frame));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.add(setBudgetBtn);
        right.add(darkBtn);
        topBar.add(right, BorderLayout.EAST);
        frame.add(topBar, BorderLayout.NORTH);

        setBudgetBtn.addActionListener(e -> {
            showSetBudgetDialog(frame);
            refreshAllPanels(dpHolder[0], tpHolder[0], epHolder[0]);
        });

        // ---- Window closing: offer to export --------------------------------
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
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
                        service.exportExpensesToCSV(path, service.getExpenses());
                    }
                }
                System.exit(0);
            }
        });

        refreshAllPanels(dpHolder[0], tpHolder[0], epHolder[0]);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** Pushes the latest data to every panel after any mutation. */
    private void refreshAllPanels(DashboardPanel dash, TrendPanel trend, ExpenseTablePanel expenses) {
        var list = service.getExpenses();
        dash.refresh(list, budgetConfig);
        trend.refresh(list);
        expenses.refresh(list);
    }

    private void showSetBudgetDialog(JFrame parent) {
        JTextField amountField   = new JTextField(10);
        String[] periods         = {"monthly", "weekly"};
        JComboBox<String> combo  = new JComboBox<>(periods);

        if (budgetConfig.isSet()) {
            amountField.setText(String.format("%.2f", budgetConfig.amount()));
            combo.setSelectedItem(budgetConfig.period());
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
        panel.add(new JLabel("Budget Amount ($):")); panel.add(amountField);
        panel.add(new JLabel("Period:"));            panel.add(combo);

        int opt = JOptionPane.showConfirmDialog(parent, panel,
                "Set Budget", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) return;

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount < 0) {
                JOptionPane.showMessageDialog(parent, "Budget cannot be negative.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            budgetConfig = new BudgetConfig(amount, (String) combo.getSelectedItem());
            service.saveBudget(budgetConfig);
            JOptionPane.showMessageDialog(parent, "Budget saved.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "Invalid amount.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void toggleDarkMode(JFrame frame) {
        darkMode = !darkMode;
        if (darkMode) FlatDarkLaf.setup();
        else          FlatLightLaf.setup();
        SwingUtilities.updateComponentTreeUI(frame);
        frame.pack();
    }
}
