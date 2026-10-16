package org.example.financemanagement.expense.dto;

import org.example.financemanagement.expense.Expense;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ExpenseDTOMapper
        implements Function<Expense, ExpenseResponse> {

    @Override
    public ExpenseResponse apply(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getExpenseDate()
        );
    }
}