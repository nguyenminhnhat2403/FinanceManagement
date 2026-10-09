package org.example.financemanagement.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserId(Long userId);
    List<Expense> findByUserIdAndExpenseDateBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}