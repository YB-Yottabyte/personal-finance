// Code for the graphical user interface (GUI) version for my application

package com.finance.manager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;



public class MainApp {

    private static final List<Expense> expenseList = new ArrayList<>();
    private static double budget = 0.0;
    private static String budgetPeriod = "";
    private static JTable expenseTable;
    private static DefaultTableModel tableModel;
    private static JTextField budgetField;
    private static JLabel remainingBudgetLabel;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::createUI);
    }

    // Method to create the user interface
    private static void createUI() {
        // Create the main window
        JFrame frame = new JFrame("Personal Finance Manager");
        JButton loadCSVButton = new JButton("Load Expenses from CSV");
        JButton exportCSVButton = new JButton("Export Expenses to CSV");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Create the table model and JTable for expenses
        String[] columnNames = {"Amount", "Category", "Date", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0);
        expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        // Buttons for adding expense, viewing budget, clearing expenses, and setting budget
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


// Action listener for the "Load CSV" button


        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Add label to show remaining budget (top)
        JPanel statusPanel = new JPanel();
        remainingBudgetLabel = new JLabel("Remaining Budget: $0.00");
        statusPanel.add(remainingBudgetLabel);
        frame.add(statusPanel, BorderLayout.NORTH);

        // Action listeners for buttons
        addExpenseButton.addActionListener(e -> addExpense());
        viewBudgetButton.addActionListener(e -> viewBudgetStatus());
        clearExpensesButton.addActionListener(e -> clearExpenses());
        setBudgetButton.addActionListener(e -> setBudget());
        loadCSVButton.addActionListener(e -> loadCSV());
        exportCSVButton.addActionListener(e -> exportCSV());

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Prompt the user to export expenses before closing the application
                int confirmation = JOptionPane.showConfirmDialog(frame,
                        "Do you want to export your expenses before closing?",
                        "Export Expenses",
                        JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    exportCSV();
                }

                // Close the application
                System.exit(0);
            }
        });






        // Display the window
        frame.setVisible(true);
    }

    // Method to add an expense
    private static void addExpense() {


        // Create a dialog to input the expense details
        JTextField amountField = new JTextField(10);
        JTextField categoryField = new JTextField(10);
        JTextField descriptionField = new JTextField(10);

        // Add industry selection
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


        // Date picker (JSpinner)
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

                // Convert the selected Date to LocalDate
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = sdf.format(selectedDate);
                LocalDate localDate = LocalDate.parse(dateString);  // Convert to LocalDate

                // Create the Expense object
                Expense expense = new Expense(amount, category, localDate, description);
                expenseList.add(expense);
                JOptionPane.showMessageDialog(null, "Expense added!");
                updateExpenseTable();
                updateRemainingBudgetLabel();  // Update the remaining budget after adding an expense
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please try again.");
            }
        }
    }

    // Method to update the expense table after adding an expense
    private static void updateExpenseTable() {
        // Clear the table before adding updated data
        tableModel.setRowCount(0);

        for (Expense expense : expenseList) {
            // Add rows to the table
            tableModel.addRow(new Object[]{
                    "$" + String.format("%.2f", expense.getAmount()),
                    expense.getCategory(),
                    expense.getDate(),
                    expense.getDescription()
            });
        }
    }

    // Method to view the current budget status
    private static void viewBudgetStatus() {
        // Display the budget status in a dialog
        double totalSpent = getTotalSpent();
        double remainingBudget = budget - totalSpent;

        String message = String.format("Budget Period: %s\nBudget Amount: $%.2f\nTotal Spent: $%.2f\nRemaining Budget: $%.2f",
                budgetPeriod, budget, totalSpent, remainingBudget);

        if (remainingBudget < 0) {
            message += "\nWarning: You have exceeded your budget!";
        }

        JOptionPane.showMessageDialog(null, message, "Budget Status", JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to calculate the total spent
    private static double getTotalSpent() {
        double totalSpent = 0.0;
        for (Expense expense : expenseList) {
            totalSpent += expense.getAmount();
        }
        return totalSpent;
    }

    // Method to set the budget
    private static void setBudget() {
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

    // Method to update the remaining budget label
    private static void updateRemainingBudgetLabel() {
        double totalSpent = getTotalSpent();
        double remainingBudget = budget - totalSpent;
        remainingBudgetLabel.setText("Remaining Budget: $" + String.format("%.2f", remainingBudget));
    }

    // Method to clear all expenses
    private static void clearExpenses() {
        int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear all expenses?", "Clear Expenses", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            expenseList.clear();
            updateExpenseTable();
            updateRemainingBudgetLabel();  // Update remaining budget after clearing expenses
            JOptionPane.showMessageDialog(null, "All expenses have been cleared.");
        }
    }

    private static void exportExpensesToCSV(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header row
            writer.write("Amount,Category,Date,Description\n");

            // Write each expense as a new line in CSV format
            for (Expense expense : expenseList) {
                writer.write(String.format("%.2f,%s,%s,%s\n",
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getDate(),
                        expense.getDescription()));
            }

            JOptionPane.showMessageDialog(null, "Expenses exported successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error exporting the CSV file: " + e.getMessage());
        }
    }

    private static void exportCSV() {
        // Open file chooser to select save location for CSV file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Expenses to CSV");
        fileChooser.setSelectedFile(new java.io.File("expenses.csv")); // Default filename

        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();

            // Check if the file has a .csv extension, if not, append it
            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }

            // Export the expenses to the selected CSV file
            exportExpensesToCSV(filePath);
        }
    }









    private static void loadExpensesFromCSV(String filePath) {
        // Clear existing expenses before loading new ones
        expenseList.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Assuming the CSV format is: Amount, Category, Date, Description
                String[] fields = line.split(",");
                if (fields.length == 4) {
                    try {
                        double amount = Double.parseDouble(fields[0].trim());
                        String category = fields[1].trim();
                        LocalDate date = LocalDate.parse(fields[2].trim()); // Assuming the date is in the format YYYY-MM-DD
                        String description = fields[3].trim();

                        // Create and add the Expense object
                        Expense expense = new Expense(amount, category, date, description);
                        expenseList.add(expense);
                    } catch (Exception ex) {
                        System.out.println("Error parsing line: " + line);
                    }
                }
            }

            // Update the UI with the loaded data
            updateExpenseTable();
            updateRemainingBudgetLabel();
            JOptionPane.showMessageDialog(null, "Expenses loaded from CSV.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading the CSV file: " + e.getMessage());
        }
    }

    private static void loadCSV() {
        // Open file chooser to select CSV file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV File");

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            loadExpensesFromCSV(filePath);
        }
    }









    // Expense class to represent individual expense items
    static class Expense {
        private final double amount;
        private final String category;
        private final LocalDate date;
        private final String description;

        public Expense(double amount, String category, LocalDate date, String description) {
            this.amount = amount;
            this.category = category;
            this.date = date;
            this.description = description;
        }

        public double getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }
    }
}
