package org.example.financemanagement.ai;

import org.example.financemanagement.category.CategoryType;

import java.math.BigDecimal;

public class ExpenseParseResult {

    private BigDecimal amount;
    private CategoryType categoryType;
    private String description;

    public ExpenseParseResult() {
    }

    public ExpenseParseResult(
            BigDecimal amount,
            CategoryType categoryType,
            String description
    ) {
        this.amount = amount;
        this.categoryType = categoryType;
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CategoryType getCategoryType() {
        return categoryType;
    }

    public String getDescription() {
        return description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}