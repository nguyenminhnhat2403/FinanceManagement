package org.example.financemanagement.expense;

import jakarta.validation.Valid;
import org.example.financemanagement.expense.dto.CreateExpenseRequest;
import org.example.financemanagement.expense.dto.ExpenseResponse;
import org.example.financemanagement.expense.dto.UpdateExpenseRequest;
import org.springframework.http.ResponseEntity;
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
    public List<ExpenseResponse> getExpenses(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam(required = false) Integer month
    ) {
        return expenseService.getExpenses(
                userId,
                year,
                month
        );
    }

    @PutMapping("/{userId}/expenses/{expenseId}")
    public ExpenseResponse updateExpense(
            @PathVariable Long userId,
            @PathVariable Long expenseId,
            @Valid @RequestBody UpdateExpenseRequest request
    ) {
        return expenseService.updateExpense(
                userId,
                expenseId,
                request
        );
    }

    @DeleteMapping("/{userId}/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long userId,
            @PathVariable Long expenseId
    ) {
        expenseService.deleteExpense(userId, expenseId);

        return ResponseEntity.noContent().build();
    }
}
