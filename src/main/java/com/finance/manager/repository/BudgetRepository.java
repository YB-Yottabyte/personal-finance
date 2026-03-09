package com.finance.manager.repository;

import com.finance.manager.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the singleton {@link Budget} row.
 * {@code save()} acts as an upsert because the id is always 1.
 */
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    // findById(1L) and save() are the only needed operations
}
