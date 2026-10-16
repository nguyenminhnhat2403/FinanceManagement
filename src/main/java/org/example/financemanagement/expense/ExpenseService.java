package org.example.financemanagement.expense;

import org.example.financemanagement.category.CategoryType;
import org.example.financemanagement.common.exception.UserNotFoundException;
import org.example.financemanagement.expense.dto.CreateExpenseRequest;
import org.example.financemanagement.expense.dto.ExpenseDTOMapper;
import org.example.financemanagement.expense.dto.ExpenseResponse;
import org.example.financemanagement.security.UserAccessService;
import org.example.financemanagement.user.User;
import org.example.financemanagement.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseDTOMapper expenseDTOMapper;
    private final UserRepository userRepository;
    private final UserAccessService userAccessService;

    public ExpenseService (ExpenseRepository expenseRepository,
                           ExpenseDTOMapper expenseDTOMapper,
                           UserRepository userRepository,
                           UserAccessService userAccessService) {
        this.expenseRepository = expenseRepository;
        this.expenseDTOMapper = expenseDTOMapper;
        this.userRepository = userRepository;
        this.userAccessService = userAccessService;
    }

    public ExpenseResponse createExpense(
            Long userId,
            CreateExpenseRequest request
    ) {
        User user = userAccessService.checkOwner(userId);

        Expense expense = new Expense(
                user,
                request.getCategoryType(),
                request.getAmount(),
                request.getDescription()
        );

        // Gán ngày chi tiêu do người dùng gửi lên
        expense.setExpenseDate(request.getExpenseDate());

        expense.setInputMethod(InputMethod.MANUAL);

        Expense savedExpense = expenseRepository.save(expense);

        return expenseDTOMapper.apply(savedExpense);
    }

    public List<ExpenseResponse> getExpensesByMonth(
            Long userId,
            int year,
            int month
    ) {
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

        List<ExpenseResponse> responses = new ArrayList<>();

        for (Expense expense : expenses) {
            responses.add(expenseDTOMapper.apply(expense));
        }

        return responses;
    }


    public ExpenseResponse createVoiceExpense(
            Long userId,
            CategoryType categoryType,
            BigDecimal amount,
            String description
    ) {

        User user = userAccessService.checkOwner(userId);

        Expense expense = new Expense(
                user,
                categoryType,
                amount,
                description
        );

        expense.setInputMethod(InputMethod.VOICE);
        Expense savedExpense = expenseRepository.save(expense);

        return expenseDTOMapper.apply(savedExpense);
    }
    // có getTotal trong report rồi
//    public BigDecimal getTotal(Long userId) {
//        BigDecimal total = new BigDecimal(0);
//        List<Expense> response = expenseRepository.findByUserId(userId);
//        for(Expense c : response) {
//            total = total.add(c.getAmount());
//        }
//        return total;
//    }

}
