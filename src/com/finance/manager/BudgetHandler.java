package com.finance.manager;

import java.security.spec.ECPoint;
import java.util.List;

public class BudgetHandler {


    public  double getRemainingBudget(double budget, double totalSpent) {
        return  (budget - totalSpent);
    }

    public  double getTotalSpent(List<Expense> expenseList) {
        double totalSpent = 0.0;
        for (com.finance.manager.Expense expense : expenseList) {
            totalSpent += expense.getAmount();
        }
        return totalSpent;
    }

}