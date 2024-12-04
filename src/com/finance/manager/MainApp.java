// Code for the graphical user interface (GUI) version for my application

package com.finance.manager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class MainApp {

    //private static final List<Expense> expenseList = new ArrayList<>();
    private static double budget = 0.0;
    private static String budgetPeriod = "";
    private  JTable expenseTable;
    private  DefaultTableModel tableModel;
    private  JTextField budgetField;
    private  JLabel remainingBudgetLabel;
    ExpenseTrackerService expenseTrackerService = new ExpenseTrackerService();

    public static void main(String[] args) {
       MainApp mainApp = new MainApp();
//       SwingUtilities.invokeLater(mainApp.createUI());
        SwingUtilities.invokeLater(mainApp::createUI);
    }

    private  void createUI() {
        JFrame frame = new JFrame("Personal Finance Manager");
        JButton loadCSVButton = new JButton("Load Expenses from CSV");
        JButton exportCSVButton = new JButton("Export Expenses to CSV");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        String[] columnNames = {"Amount", "Category", "Date", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0);
        expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton addExpenseButton = new JButton("Add Expense");
        JButton viewBudgetButton = new JButton("View Budget");
        JButton clearExpensesButton = new JButton("Clear Expenses");
        JButton setBudgetButton = new JButton("Set Budget");

        buttonPanel.add(addExpenseButton);
        buttonPanel.add(viewBudgetButton);
        buttonPanel.add(clearExpensesButton);
        buttonPanel.add(setBudgetButton);
        buttonPanel.add(loadCSVButton);
        buttonPanel.add(exportCSVButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        JPanel statusPanel = new JPanel();
        remainingBudgetLabel = new JLabel("Remaining Budget: $0.00");
        statusPanel.add(remainingBudgetLabel);
        frame.add(statusPanel, BorderLayout.NORTH);

        addExpenseButton.addActionListener(e -> addExpense());
        viewBudgetButton.addActionListener(e -> viewBudgetStatus());
        clearExpensesButton.addActionListener(e -> clearExpenses());
        setBudgetButton.addActionListener(e -> setBudget());
        loadCSVButton.addActionListener(e -> loadCSV());
        exportCSVButton.addActionListener(e -> exportCSV());

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int confirmation = JOptionPane.showConfirmDialog(frame,
                        "Do you want to export your expenses before closing?",
                        "Export Expenses",
                        JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    exportCSV();
                }

                System.exit(0);
            }
        });
        frame.setVisible(true);
    }

    private  void addExpense() {


        JTextField amountField = new JTextField(10);
        JTextField categoryField = new JTextField(10);
        JTextField descriptionField = new JTextField(10);

        String[] industries = {"Retail", "Technology", "Healthcare", "Manufacturing", "Services"};
        JComboBox<String> industryComboBox = new JComboBox<>(industries);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Industry:"));
        panel.add(industryComboBox);  // Add industry dropdown
        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);


        JPanel datePanel = new JPanel();
        JLabel dateLabel = new JLabel("Date (Select from Calendar):");
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        datePanel.add(dateLabel);
        datePanel.add(dateSpinner);
        panel.add(datePanel);

        int option = JOptionPane.showConfirmDialog(null, panel, "Enter Expense Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String category = categoryField.getText();
                String description = descriptionField.getText();
                Date selectedDate = (Date) dateSpinner.getValue();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = sdf.format(selectedDate);
                LocalDate localDate = LocalDate.parse(dateString);  // Convert to LocalDate

                com.finance.manager.Expense expense = new com.finance.manager.Expense(amount, category, localDate, description);
                expenseTrackerService.addExpense(expense);
                JOptionPane.showMessageDialog(null, "Expense added!");
                updateExpenseTable();
                updateRemainingBudgetLabel();  // Update the remaining budget after adding an expense
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please try again.");
            }
        }
    }

    private  void updateExpenseTable() {
        tableModel.setRowCount(0);
        List<com.finance.manager.Expense> expenseList = expenseTrackerService.getExpenses();
        for (var expense : expenseList) {
            tableModel.addRow(new Object[]{
                    "$" + String.format("%.2f", expense.getAmount()),
                    expense.getCategory(),
                    expense.getDate(),
                    expense.getDescription()
            });
        }
    }

    private  void viewBudgetStatus() {
        double totalSpent = expenseTrackerService.getTotalSpent();
        double remainingBudget = expenseTrackerService.getRemainingBudget(budget);

        String message = String.format("Budget Period: %s\nBudget Amount: $%.2f\nTotal Spent: $%.2f\nRemaining Budget: $%.2f",
                budgetPeriod, budget, totalSpent, remainingBudget);

        if (remainingBudget < 0) {
            message += "\nWarning: You have exceeded your budget!";
        }

        JOptionPane.showMessageDialog(null, message, "Budget Status", JOptionPane.INFORMATION_MESSAGE);
    }

  /*  private static double getTotalSpent() {
        double totalSpent = 0.0;
        var expenseList =ExpenseManager.getExpenses();
        for (com.finance.manager.Expense expense : expenseList) {
            totalSpent += expense.getAmount();
        }
        return totalSpent;
    }*/


    private  void setBudget() {
        JTextField budgetAmountField = new JTextField(10);
        JTextField periodField = new JTextField(10);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));
        panel.add(new JLabel("Budget Amount ($):"));
        panel.add(budgetAmountField);
        panel.add(new JLabel("Budget Period (weekly/monthly):"));
        panel.add(periodField);

        int option = JOptionPane.showConfirmDialog(null, panel, "Set Budget", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            try {
                budget = Double.parseDouble(budgetAmountField.getText());
                budgetPeriod = periodField.getText().toLowerCase();

                if (!budgetPeriod.equals("weekly") && !budgetPeriod.equals("monthly")) {
                    JOptionPane.showMessageDialog(null, "Invalid budget period. Please enter 'weekly' or 'monthly'.");
                    return;
                }

                // Update the remaining budget label
                updateRemainingBudgetLabel();
                JOptionPane.showMessageDialog(null, "Budget set successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid amount. Please try again.");
            }
        }
    }


    private  void updateRemainingBudgetLabel() {
        double remainingBudget = expenseTrackerService.getRemainingBudget(budget);
        remainingBudgetLabel.setText("Remaining Budget: $" + String.format("%.2f", remainingBudget));
    }


    private  void clearExpenses() {
        int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear all expenses?", "Clear Expenses", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
//            expenseList.clear();
            ExpenseHandler.clearExpenses();
            updateExpenseTable();
            updateRemainingBudgetLabel();  // Update remaining budget after clearing expenses
            JOptionPane.showMessageDialog(null, "All expenses have been cleared.");
        }
    }


    private  void exportCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Expenses to CSV");
        fileChooser.setSelectedFile(new java.io.File("expenses.csv")); // Default filename

        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();

            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }
            var expensesList  = expenseTrackerService.getExpenses();
            expenseTrackerService.exportExpensesToCSV(filePath, expensesList );
        }
    }

    private  void loadExpensesFromCSVUI(String filePath) {

        ExpenseHandler.clearExpenses();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
           /* String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length == 4) {
                    try {
                        double amount = Double.parseDouble(fields[0].trim());
                        String category = fields[1].trim();
                        LocalDate date = LocalDate.parse(fields[2].trim()); // Assuming the date is in the format YYYY-MM-DD
                        String description = fields[3].trim();

                        com.finance.manager.Expense expense = new com.finance.manager.Expense(amount, category, date, description);
                        ExpenseManager.addExpense(expense);
                    } catch (Exception ex) {
                        System.out.println("Error parsing line: " + line);
                    }
                }
            }
*/
            expenseTrackerService.loadExpensesFromCSV(filePath);
            updateExpenseTable();
            updateRemainingBudgetLabel();
            JOptionPane.showMessageDialog(null, "Expenses loaded from CSV.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading the CSV file: " + e.getMessage());
        }
    }

    private  void loadCSV() {
        // Open file chooser to select CSV file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV File");

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            loadExpensesFromCSVUI(filePath);

        }
    }




}