package org.example.financemanagement.report;

import org.example.financemanagement.common.exception.UserNotFoundException;
import org.example.financemanagement.expense.Expense;
import org.example.financemanagement.expense.ExpenseRepository;
import org.example.financemanagement.user.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ReportService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public BigDecimal getTotalExpense(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        BigDecimal total = BigDecimal.ZERO;

        for (Expense expense : expenses) total = total.add(expense.getAmount());

        return total;
    }

    public BigDecimal getMonthlyExpense(
            Long userId,
            int year,
            int month
    ) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(
                                userId,
                                start,
                                end
                        );

        BigDecimal total = BigDecimal.ZERO;
        for (Expense expense : expenses) total = total.add(expense.getAmount());

        return total;
    }
}