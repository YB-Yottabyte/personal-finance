package com.finance.manager.service;

import com.finance.manager.CSVHandler;
import com.finance.manager.Expense;
import com.finance.manager.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for all expense operations.
 * Annotated with {@link Transactional} so every write is atomic —
 * no partial saves on failure.
 */
@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CSVHandler csvHandler;

    public ExpenseService(ExpenseRepository expenseRepository, CSVHandler csvHandler) {
        this.expenseRepository = expenseRepository;
        this.csvHandler        = csvHandler;
    }

    // ---- Write operations ---------------------------------------------------

    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    public void clearAll() {
        expenseRepository.deleteAll();
    }

    /** Replaces all expenses with rows loaded from the given CSV file. */
    public void importFromCSV(String filePath) {
        List<Expense> loaded = csvHandler.loadExpensesFromCSV(filePath);
        expenseRepository.deleteAll();
        expenseRepository.saveAll(loaded);
    }

    // ---- Read operations ----------------------------------------------------

    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAllByOrderByDateDescIdDesc();
    }

    // ---- Export -------------------------------------------------------------

    public void exportToCSV(String filePath) {
        csvHandler.exportExpensesToCSV(filePath, getAllExpenses());
    }
}
