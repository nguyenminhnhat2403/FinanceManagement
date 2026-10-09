package org.example.financemanagement.expense.dto;

import org.example.financemanagement.category.CategoryType;

import java.math.BigDecimal;

public class ExpenseResponse {
    private Long id;
    private CategoryType category;
    private BigDecimal amount;
    private String description;

    public ExpenseResponse(Long id,
                           CategoryType category,
                           BigDecimal amount,
                           String description) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public CategoryType getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}
