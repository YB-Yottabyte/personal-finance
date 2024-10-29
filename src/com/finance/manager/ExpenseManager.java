package com.finance.manager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseManager {
    private List<Expense> expenses;
    private final String filePath = "expenses.csv";
    private double budget;
    private String budgetPeriod;

    public ExpenseManager() {
        expenses = new ArrayList<>();
        loadExpenses();
    }

    // Add an expense to the list
    public void addExpense(Expense expense) {
        expenses.add(expense);
        saveExpenses();
    }

    // Get the list of expenses
    public List<Expense> getExpenses() {
        return expenses;
    }

    public String getBudgetPeriod() {
        return budgetPeriod;
    }

    public double getBudget() {
        return budget;
    }


    public void setBudget(double amount, String period) {
        this.budget = amount;
        this.budgetPeriod = period;
    }

    public double getTotalSpent(){
        double sum = 0.0;
        for(Expense expense : expenses){
            sum+=expense.getAmount();
        }
        return sum;
    }

    public double getRemainingBudget(){
        return budget - getTotalSpent();
    }

    // Save expenses to a CSV file
    private void saveExpenses() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Expense expense : expenses) {
                writer.write(expense.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving expenses: " + e.getMessage());
        }
    }

    // Load expenses from a CSV file
    private void loadExpenses() {
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Expense expense = Expense.fromCSV(line);
                    expenses.add(expense);
                }
            } catch (IOException e) {
                System.out.println("Error loading expenses: " + e.getMessage());
            }
        }
    }

    // Method to clear all expenses
    public void clearExpenses() {
        expenses.clear(); // Clear the list of expenses
        // Clear the CSV file by writing an empty string to it
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(""); // Write an empty string to clear the file
        } catch (IOException e) {
            System.out.println("Error clearing expenses: " + e.getMessage());
        }
        System.out.println("All expenses cleared.");
    }
}


