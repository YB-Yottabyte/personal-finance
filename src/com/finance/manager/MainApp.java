package com.finance.manager;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        ExpenseManager expenseManager = new ExpenseManager();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Welcome to Personal Finance Manager");
            System.out.println("1. Add Expense");
            System.out.println("2. View Expenses");
            System.out.println("3. Clear Expenses");
            System.out.println("4. Set Budget");
            System.out.println("5. View Budget");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); 
                continue; 
            }

            switch (choice) {
                case 1:
                    AddExpense(scanner, expenseManager);
                    break;

                case 2:
                    viewExpenses(expenseManager);
                    break;

                case 3:
                    ClearExpense(scanner, expenseManager);
                    break;

                case 4:
                    setBudget(scanner, expenseManager);
                    break;

                case 5:
                    viewBudgetStatus(expenseManager);
                    break;

                case 6:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void AddExpense(Scanner scanner, ExpenseManager expenseManager) {
        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();
        System.out.print("Enter category: ");
        String category = scanner.next();
        System.out.print("Enter date (YYYY-MM-DD): ");
        String dateInput = scanner.next();
        LocalDate date;

        try {
            date = LocalDate.parse(dateInput); // Parse to LocalDate
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return;
        }

        System.out.print("Enter description: ");
        String description = scanner.next();

        // Create Expense object and add it to the manager
        Expense expense = new Expense(amount, category, date, description);
        expenseManager.addExpense(expense);
        System.out.println("Expense added!");
    }


    private static void viewExpenses(ExpenseManager expenseManager) {
        System.out.printf("%-10s %-15s %-12s %-20s%n", "Amount", "Category", "Date", "Description");
        System.out.println("---------------------------------------------------------------");

        for (Expense exp : expenseManager.getExpenses()) {
            System.out.printf("%-10.2f %-15s %-12s %-20s%n",
                    exp.getAmount(), exp.getCategory(), exp.getDate(), exp.getDescription());
        }
    }

    private static void setBudget(Scanner scanner, ExpenseManager expenseManager) {
        System.out.print("Enter budget amount: ");
        double amount = scanner.nextDouble();
        System.out.print("Enter budget period (weekly/monthly): ");
        String period = scanner.next().toLowerCase();

        if(!period.equals("weekly") && !period.equals("monthly")) {
            System.out.println("Invalid budget period. Please use weekly/monthly.");
            return;
        }

        expenseManager.setBudget(amount, period);
        System.out.println("Budget set to " + amount +" for " + period + " period.");
    }

    private static void viewBudgetStatus(ExpenseManager expenseManager) {
        double totalSpent = expenseManager.getTotalSpent();
        double remainingBudget = expenseManager.getRemainingBudget();
        String budgetPeriod = expenseManager.getBudgetPeriod();

        System.out.println("Budget Period: " + budgetPeriod);
        System.out.println("Budget Amount: " + expenseManager.getBudget());
        System.out.printf("Total Spent: %.2f%n", totalSpent);
        System.out.printf("Remaining Budget: %.2f%n", remainingBudget);

        if (remainingBudget < 0) {
            System.out.println("Warning: You have exceeded your budget!");
        }
    }

    private static void ClearExpense(Scanner scanner, ExpenseManager expenseManager) {
        System.out.print("Are you sure you want to clear all expenses? (yes/no): ");
        String confirmation = scanner.next();
        if (confirmation.equalsIgnoreCase("yes")) {
            expenseManager.clearExpenses();
        } else {
            System.out.println("Clearing expenses aborted.");
        }
    }
    
}