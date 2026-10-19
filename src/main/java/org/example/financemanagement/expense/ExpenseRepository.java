package org.example.financemanagement.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserId(Long userId);

    @Query("""
            SELECT e
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.expenseDate >= :start
              AND e.expenseDate < :end
            ORDER BY e.expenseDate DESC, e.id DESC
            """)
    List<Expense> findExpensesInPeriod(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    @Query("""
        SELECT SUM(e.amount)
        FROM Expense e
        WHERE e.user.id = :userId
        """)
    BigDecimal sumAmountByUserId(
            @Param("userId") Long userId
    );
    Optional<Expense> findByIdAndUserId(
            Long expenseId,
            Long userId
    );
}