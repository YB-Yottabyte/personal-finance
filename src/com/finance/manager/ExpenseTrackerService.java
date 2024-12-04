package com.finance.manager;

import java.util.List;

public class ExpenseTrackerService {
    BudgetHandler budgetHandler= new BudgetHandler();
    CSVHandler csvHandler= new CSVHandler();
    ExpenseHandler expenseHandler= new ExpenseHandler();

    public double getRemainingBudget(double budget){
        double totalSpent = getTotalSpent();
        return budgetHandler.getRemainingBudget(budget,totalSpent);
    }

    public double getTotalSpent(){
        var expensesList = expenseHandler.getExpenses();
        return budgetHandler.getTotalSpent(expensesList);
    }

    public void exportExpensesToCSV(String filePath, List<Expense> expenseList){


        csvHandler.exportExpensesToCSV(filePath, expenseList);
    }

    public void loadExpensesFromCSV(String filePath){

        csvHandler.loadExpensesFromCSV(filePath);
    }

    public void addExpense(Expense expense){
        expenseHandler.addExpense(expense);
    }

    public  List<Expense> getExpenses() {
        return expenseHandler.getExpenses();
    }

    public void clearExpenses(){
        expenseHandler.clearExpenses();
   }


}
