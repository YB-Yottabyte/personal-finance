package com.finance.manager;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the in-memory expense list and persists changes to a CSV file after
 * every mutation. All state is instance-based (not static) so the class is
 * testable in isolation and free from shared-state bugs.
 */
public class ExpenseHandler {

    private static final String DEFAULT_FILE = "expenses.csv";
    private final List<Expense> expenses = new ArrayList<>();

    public ExpenseHandler() {
        loadExpenses();
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        saveExpenses();
    }

    /** Returns an unmodifiable view of all expenses. */
    public List<Expense> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public void clearExpenses() {
        expenses.clear();
        saveExpenses();
    }

    private void saveExpenses() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DEFAULT_FILE))) {
            for (Expense expense : expenses) {
                writer.write(expense.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving expenses: " + e.getMessage());
        }
    }

    private void loadExpenses() {
        File file = new File(DEFAULT_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    expenses.add(Expense.fromCSV(line));
                } catch (Exception e) {
                    System.err.println("Skipping malformed line on load: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading expenses: " + e.getMessage());
        }
    }
}
