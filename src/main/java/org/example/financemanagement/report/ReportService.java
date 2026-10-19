package org.example.financemanagement.report;

import org.example.financemanagement.common.exception.UserNotFoundException;
import org.example.financemanagement.expense.Expense;
import org.example.financemanagement.expense.ExpenseRepository;
import org.example.financemanagement.security.UserAccessService;
import org.example.financemanagement.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final UserAccessService userAccessService;
    public ReportService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            UserAccessService userAccessService
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.userAccessService = userAccessService;
    }

    public BigDecimal getTotalExpense(Long userId) {
        userAccessService.checkOwner(userId);

        BigDecimal total =
                expenseRepository.sumAmountByUserId(userId);

        // SUM trả null khi không có khoản chi phù hợp
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getMonthlyExpense(
            Long userId,
            int year,
            int month
    ) {
        // Kiểm tra user đang đăng nhập có được xem dữ liệu này không
        userAccessService.checkOwner(userId);

        if (year < 1 || year > 9999) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Năm phải nằm trong khoảng từ 1 đến 9999"
            );
        }

        if (month < 1 || month > 12) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tháng phải nằm trong khoảng từ 1 đến 12"
            );
        }

        LocalDateTime start = LocalDateTime.of(
                year, month, 1, 0, 0
        );

        LocalDateTime end = start.plusMonths(1);

        List<Expense> expenses =
                expenseRepository.findExpensesInPeriod(
                        userId,
                        start,
                        end
                );

        BigDecimal total = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            total = total.add(expense.getAmount());
        }

        return total;
    }
}