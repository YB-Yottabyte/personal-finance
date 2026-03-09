package com.finance.manager.ui;

import com.finance.manager.Expense;
import com.finance.manager.service.ExpenseService;
import com.finance.manager.analytics.SpendingAnomaly;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Expenses tab: searchable, sortable, color-coded expense table with
 * Add / Clear / Load CSV / Export CSV actions.
 */
public class ExpenseTablePanel extends JPanel {

    private static final String[] COLUMNS = {"Amount", "Category", "Date", "Description"};
    private static final String[] CATEGORIES = {
            "Food", "Transport", "Housing", "Healthcare",
            "Entertainment", "Shopping", "Technology", "Other"
    };

    private final ExpenseService service;
    private final Runnable       onDataChanged;

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) { return String.class; }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final JTextField searchField = new JTextField(20);

    private Set<Integer> anomalyRows = Set.of();

    public ExpenseTablePanel(ExpenseService service, Runnable onDataChanged) {
        this.service       = service;
        this.onDataChanged = onDataChanged;
        setLayout(new BorderLayout(6, 6));
        setBorder(new EmptyBorder(8, 10, 8, 10));

        // Search bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topBar.add(new JLabel("Search:"));
        topBar.add(searchField);
        JButton clearSearch = new JButton("✕");
        clearSearch.setToolTipText("Clear search");
        clearSearch.addActionListener(e -> searchField.setText(""));
        topBar.add(clearSearch);
        add(topBar, BorderLayout.NORTH);

        // Table
        table.setRowSorter(sorter);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);

        // Zebra stripes + anomaly highlight
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean selected, boolean focused, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focused, row, col);
                if (!selected) {
                    int modelRow = table.convertRowIndexToModel(row);
                    if (anomalyRows.contains(modelRow)) {
                        c.setBackground(new Color(255, 220, 220));
                    } else if (row % 2 == 0) {
                        c.setBackground(UIManager.getColor("Table.background"));
                    } else {
                        Color base = UIManager.getColor("Table.background");
                        c.setBackground(base == null ? new Color(245, 245, 250)
                                : base.darker().brighter().brighter());
                    }
                }
                return c;
            }
        });

        // Live search
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        JButton addBtn    = new JButton("Add Expense");
        JButton clearBtn  = new JButton("Clear All");
        JButton loadBtn   = new JButton("Load CSV");
        JButton exportBtn = new JButton("Export CSV");
        buttons.add(addBtn); buttons.add(clearBtn);
        buttons.add(loadBtn); buttons.add(exportBtn);
        add(buttons, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddExpenseDialog());
        clearBtn.addActionListener(e -> clearExpenses());
        loadBtn.addActionListener(e -> loadCSV());
        exportBtn.addActionListener(e -> exportCSV());
    }

    // ---- Refresh ------------------------------------------------------------

    public void refresh(List<Expense> expenses) {
        SpendingAnomaly detector = new SpendingAnomaly();
        Set<Expense> flagged = detector.detectAnomalies(expenses).stream()
                .map(SpendingAnomaly.AnomalyResult::expense)
                .collect(Collectors.toSet());
        anomalyRows = java.util.stream.IntStream.range(0, expenses.size())
                .filter(i -> flagged.contains(expenses.get(i)))
                .boxed()
                .collect(Collectors.toSet());

        tableModel.setRowCount(0);
        for (Expense e : expenses) {
            tableModel.addRow(new Object[]{
                    String.format("$%.2f", e.getAmount()),
                    e.getCategory(),
                    e.getDate().toString(),
                    e.getDescription()
            });
        }
        applyFilter();
    }

    // ---- Actions ------------------------------------------------------------

    private void showAddExpenseDialog() {
        JTextField amountField    = new JTextField(10);
        JTextField descField      = new JTextField(20);
        JComboBox<String> catCombo = new JComboBox<>(CATEGORIES);
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner      = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

        JPanel panel = new JPanel(new GridLayout(4, 2, 6, 6));
        panel.add(new JLabel("Amount ($):")); panel.add(amountField);
        panel.add(new JLabel("Category:"));  panel.add(catCombo);
        panel.add(new JLabel("Date:"));      panel.add(dateSpinner);
        panel.add(new JLabel("Description:")); panel.add(descField);

        int opt = JOptionPane.showConfirmDialog(this, panel,
                "Add Expense", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) return;

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount < 0) { showWarning("Amount cannot be negative."); return; }
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format((Date) dateSpinner.getValue());
            service.addExpense(new Expense(amount, (String) catCombo.getSelectedItem(),
                    LocalDate.parse(dateStr), descField.getText().trim()));
            onDataChanged.run();
        } catch (NumberFormatException ex) {
            showWarning("Please enter a valid number for the amount.");
        }
    }

    private void clearExpenses() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Clear all expenses? This cannot be undone.",
                "Confirm Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            service.clearAll();
            onDataChanged.run();
        }
    }

    private void loadCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load Expenses from CSV");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            service.importFromCSV(fc.getSelectedFile().getAbsolutePath());
            onDataChanged.run();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Expenses to CSV");
        fc.setSelectedFile(new java.io.File("expenses.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".csv")) path += ".csv";
        try {
            service.exportToCSV(path);
            JOptionPane.showMessageDialog(this, "Exported to:\n" + path);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}
