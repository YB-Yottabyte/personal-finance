package com.finance.manager;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Facade coordinating all finance operations. Supports constructor injection
 * so handlers can be replaced with test doubles in unit tests.
 */
public class ExpenseTrackerService {

    private static final String BUDGET_FILE = "budget.properties";

    private final BudgetHandler budgetHandler;
    private final CSVHandler csvHandler;
    private final ExpenseHandler expenseHandler;

    /** Production constructor — wires up real handler instances. */
    public ExpenseTrackerService() {
        this(new BudgetHandler(), new CSVHandler(), new ExpenseHandler());
    }

    /** Injection constructor used in unit tests. */
    public ExpenseTrackerService(BudgetHandler budgetHandler,
                                  CSVHandler csvHandler,
                                  ExpenseHandler expenseHandler) {
        this.budgetHandler = budgetHandler;
        this.csvHandler = csvHandler;
        this.expenseHandler = expenseHandler;
    }

    // ---- Expense operations -------------------------------------------------

    public void addExpense(Expense expense) {
        expenseHandler.addExpense(expense);
    }

    public List<Expense> getExpenses() {
        return expenseHandler.getExpenses();
    }

    public void clearExpenses() {
        expenseHandler.clearExpenses();
    }

    // ---- Budget operations --------------------------------------------------

    public double getTotalSpent() {
        return budgetHandler.getTotalSpent(expenseHandler.getExpenses());
    }

    public double getRemainingBudget(double budget) {
        return budgetHandler.getRemainingBudget(budget, getTotalSpent());
    }

    /** Persists a {@link BudgetConfig} to disk. */
    public void saveBudget(BudgetConfig config) {
        Properties props = new Properties();
        props.setProperty("amount", String.valueOf(config.amount()));
        props.setProperty("period", config.period());
        try (Writer w = new FileWriter(BUDGET_FILE)) {
            props.store(w, "Budget settings");
        } catch (IOException e) {
            System.err.println("Could not save budget: " + e.getMessage());
        }
    }

    /** Loads budget settings from disk, returning {@link BudgetConfig#UNSET} if absent. */
    public BudgetConfig loadBudget() {
        File file = new File(BUDGET_FILE);
        if (!file.exists()) return BudgetConfig.UNSET;
        Properties props = new Properties();
        try (Reader r = new FileReader(file)) {
            props.load(r);
            double amount = Double.parseDouble(props.getProperty("amount", "0.0"));
            String period = props.getProperty("period", "");
            return new BudgetConfig(amount, period);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Could not load budget: " + e.getMessage());
            return BudgetConfig.UNSET;
        }
    }

    // ---- CSV operations -----------------------------------------------------

    public void exportExpensesToCSV(String filePath, List<Expense> expenseList) {
        csvHandler.exportExpensesToCSV(filePath, expenseList);
    }

    /** Replaces the current expense list with entries loaded from the given CSV file. */
    public void loadExpensesFromCSV(String filePath) {
        List<Expense> loaded = csvHandler.loadExpensesFromCSV(filePath);
        expenseHandler.clearExpenses();
        loaded.forEach(expenseHandler::addExpense);
    }
}
