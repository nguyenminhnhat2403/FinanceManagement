package org.example.financemanagement.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.financemanagement.category.CategoryType;

import java.math.BigDecimal;

public class CreateExpenseRequest {

    @NotNull
    private CategoryType categoryType;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    @NotNull
    private String description;

    public CategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
