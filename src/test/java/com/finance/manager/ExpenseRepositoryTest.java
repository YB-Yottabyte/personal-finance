package com.finance.manager;

import com.finance.manager.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA slice test for {@link ExpenseRepository}.
 * {@link DataJpaTest} spins up an in-memory H2 database with Hibernate —
 * no application context, no Kafka, no Swing.
 */
@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository repo;

    private static final LocalDate TODAY     = LocalDate.of(2024, 6, 15);
    private static final LocalDate YESTERDAY = LocalDate.of(2024, 6, 14);

    @BeforeEach
    void clearTable() {
        repo.deleteAll();
    }

    @Test
    void save_assignsGeneratedId() {
        Expense saved = repo.save(new Expense(10.0, "Food", TODAY, "lunch"));
        assertThat(saved.getId()).isPositive();
    }

    @Test
    void save_persistsAllFields() {
        repo.save(new Expense(42.50, "Transport", TODAY, "taxi"));

        List<Expense> all = repo.findAll();
        assertThat(all).hasSize(1);
        Expense e = all.get(0);
        assertThat(e.getAmount()).isEqualTo(42.50);
        assertThat(e.getCategory()).isEqualTo("Transport");
        assertThat(e.getDate()).isEqualTo(TODAY);
        assertThat(e.getDescription()).isEqualTo("taxi");
    }

    @Test
    void findAllByOrderByDateDescIdDesc_returnsMostRecentFirst() {
        repo.save(new Expense(1.0, "Food",      YESTERDAY, "old"));
        repo.save(new Expense(2.0, "Transport", TODAY,     "new"));

        List<Expense> result = repo.findAllByOrderByDateDescIdDesc();
        assertThat(result.get(0).getDate()).isEqualTo(TODAY);
    }

    @Test
    void findByCategoryIgnoreCase_filtersCorrectly() {
        repo.save(new Expense(5.0,  "food",  TODAY, "a"));
        repo.save(new Expense(10.0, "Rent",  TODAY, "b"));

        List<Expense> food = repo.findByCategoryIgnoreCase("FOOD");
        assertThat(food).hasSize(1);
        assertThat(food.get(0).getDescription()).isEqualTo("a");
    }

    @Test
    void sumAllAmounts_returnsCorrectTotal() {
        repo.save(new Expense(10.0, "Food",   TODAY, ""));
        repo.save(new Expense(25.0, "Rent",   TODAY, ""));
        repo.save(new Expense(15.5, "Coffee", TODAY, ""));

        assertThat(repo.sumAllAmounts()).isEqualTo(50.5);
    }

    @Test
    void sumAllAmounts_returnsZeroWhenEmpty() {
        assertThat(repo.sumAllAmounts()).isEqualTo(0.0);
    }

    @Test
    void deleteAll_removesEveryRow() {
        repo.save(new Expense(5.0, "Food",   TODAY, "a"));
        repo.save(new Expense(10.0, "Coffee", TODAY, "b"));

        repo.deleteAll();

        assertThat(repo.count()).isZero();
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    void deleteById_removesOnlyTargetRow() {
        Expense kept    = repo.save(new Expense(10.0, "Food", TODAY, "keep"));
        Expense deleted = repo.save(new Expense(20.0, "Rent", TODAY, "delete"));

        repo.deleteById(deleted.getId());

        List<Expense> remaining = repo.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getId()).isEqualTo(kept.getId());
    }
}
