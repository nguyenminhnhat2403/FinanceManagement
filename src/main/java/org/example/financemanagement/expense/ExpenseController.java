package org.example.financemanagement.expense;

import jakarta.validation.Valid;
import org.example.financemanagement.expense.dto.CreateExpenseRequest;
import org.example.financemanagement.expense.dto.ExpenseResponse;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }


    @PostMapping("/{userId}/expenses")
    public ExpenseResponse createExpense(
            @PathVariable Long userId,
            @Valid @RequestBody CreateExpenseRequest request
    ) {
        return expenseService.createExpense(userId, request);
    }

    @GetMapping("/{userId}/expenses")
    public List<ExpenseResponse> getExpensesByMonth(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return expenseService.getExpensesByMonth(
                userId,
                year,
                month
        );
    }



}
