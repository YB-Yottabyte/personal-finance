package com.finance.manager;

import com.sun.source.doctree.ThrowsTree;

import javax.swing.*;
import java.io.*;
import java.time.LocalDate;
import java.util.List;


public class CSVHandler {

    public  void exportExpensesToCSV(String filePath, List<Expense> expernseList) {
//        var expenseList = getExpenses();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            writer.write("Amount,Category,Date,Description\n");

            for (var expense : expernseList) {
                writer.write(String.format("%.2f,%s,%s,%s\n",
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getDate(),
                        expense.getDescription()));
            }

//            JOptionPane.showMessageDialog(null, "Expenses exported successfully!");
        } catch (IOException e) {
//            JOptionPane.showMessageDialog(null, "Error exporting the CSV file: " + e.getMessage());
            throw new RuntimeException("Error exporting the CSV file: " + e.getMessage());
        }
    }

    public  void loadExpensesFromCSV(String filePath) {

        ExpenseHandler.clearExpenses();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length == 4) {
                    try {
                        double amount = Double.parseDouble(fields[0].trim());
                        String category = fields[1].trim();
                        LocalDate date = LocalDate.parse(fields[2].trim()); // Assuming the date is in the format YYYY-MM-DD
                        String description = fields[3].trim();

                        com.finance.manager.Expense expense = new com.finance.manager.Expense(amount, category, date, description);
                        ExpenseHandler.addExpense(expense);
                    } catch (Exception ex) {
                        System.out.println("Error parsing line: " + line);
                    }
                }
            }

           // updateExpenseTable();
           // updateRemainingBudgetLabel();
            //JOptionPane.showMessageDialog(null, "Expenses loaded from CSV.");
        } catch (IOException e) {
            //JOptionPane.showMessageDialog(null, "Error reading the CSV file: " + e.getMessage());
        }
    }

}
