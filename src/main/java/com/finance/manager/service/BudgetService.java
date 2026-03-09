package com.finance.manager.service;

import com.finance.manager.Budget;
import com.finance.manager.BudgetConfig;
import com.finance.manager.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for budget persistence.
 * Uses Spring Data's upsert-by-id: {@code save()} with id=1 always
 * updates the single budget row rather than inserting a duplicate.
 */
@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public void save(BudgetConfig config) {
        budgetRepository.save(Budget.from(config));
    }

    @Transactional(readOnly = true)
    public BudgetConfig load() {
        return budgetRepository.findById(1L)
                .map(Budget::toConfig)
                .orElse(BudgetConfig.UNSET);
    }
}
