package org.example.financemanagement.expense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.financemanagement.category.CategoryType;

import java.math.BigDecimal;

public class UpdateExpenseRequest {

    @NotNull
    private CategoryType categoryType;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String description;

    public UpdateExpenseRequest() {
    }

    public CategoryType getCategoryType() {
        return categoryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}