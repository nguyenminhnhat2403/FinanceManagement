package org.example.financemanagement.expense;

import org.example.financemanagement.common.exception.UserNotFoundException;
import org.example.financemanagement.expense.dto.CreateExpenseRequest;
import org.example.financemanagement.expense.dto.ExpenseDTOMapper;
import org.example.financemanagement.expense.dto.ExpenseResponse;
import org.example.financemanagement.user.User;
import org.example.financemanagement.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseDTOMapper expenseDTOMapper;
    private final UserRepository userRepository;

    public ExpenseService (ExpenseRepository expenseRepository,
                           ExpenseDTOMapper expenseDTOMapper,
                           UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseDTOMapper = expenseDTOMapper;
        this.userRepository = userRepository;
    }

    public ExpenseResponse createExpense(Long userId,
                                         CreateExpenseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Expense expense = new Expense(
                user,
                request.getCategoryType(),
                request.getAmount(),
                request.getDescription()
        );
        expense.setInputMethod(InputMethod.MANUAL);
        Expense savedExpense = expenseRepository.save(expense);
        return expenseDTOMapper.apply(savedExpense);
    }

    public List<ExpenseResponse> getAllExpense(Long userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        List<ExpenseResponse> responses = new ArrayList<>();
        for(Expense c : expenses) {
            responses.add(expenseDTOMapper.apply(c));
        }
        return responses;

    }


}
