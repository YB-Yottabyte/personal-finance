package com.finance.manager;

import com.finance.manager.repository.BudgetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA slice test for {@link BudgetRepository}.
 */
@DataJpaTest
class BudgetRepositoryTest {

    @Autowired
    private BudgetRepository repo;

    @Test
    void findById_returnsEmptyWhenNothingSaved() {
        assertThat(repo.findById(1L)).isEmpty();
    }

    @Test
    void save_thenLoad_roundTrips() {
        repo.save(new Budget(1500.0, "monthly"));

        Optional<Budget> loaded = repo.findById(1L);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getAmount()).isEqualTo(1500.0);
        assertThat(loaded.get().getPeriod()).isEqualTo("monthly");
    }

    @Test
    void save_secondCall_upserts_noDuplicate() {
        repo.save(new Budget(500.0, "weekly"));
        repo.save(new Budget(2000.0, "monthly"));

        assertThat(repo.count()).isEqualTo(1);
        Optional<Budget> loaded = repo.findById(1L);
        assertThat(loaded.get().getAmount()).isEqualTo(2000.0);
        assertThat(loaded.get().getPeriod()).isEqualTo("monthly");
    }

    @Test
    void toConfig_mapsFieldsCorrectly() {
        Budget budget = new Budget(1200.0, "monthly");
        BudgetConfig config = budget.toConfig();

        assertThat(config.amount()).isEqualTo(1200.0);
        assertThat(config.period()).isEqualTo("monthly");
        assertThat(config.isSet()).isTrue();
    }
}
