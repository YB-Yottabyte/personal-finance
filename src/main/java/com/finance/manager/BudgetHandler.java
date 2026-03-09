package com.finance.manager;

import java.util.List;

public class BudgetHandler {

    public double getRemainingBudget(double budget, double totalSpent) {
        return budget - totalSpent;
    }

    public double getTotalSpent(List<Expense> expenseList) {
        return expenseList.stream().mapToDouble(Expense::getAmount).sum();
    }
}