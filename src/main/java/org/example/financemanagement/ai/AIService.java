package org.example.financemanagement.ai;

import org.example.financemanagement.category.CategoryType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AIService {

    public ExpenseParseResult parseExpense(String text) {

        String lowerText = text.toLowerCase();

        if (lowerText.contains("cafe")) {
            return new ExpenseParseResult(
                    new BigDecimal("30000"),
                    CategoryType.FOOD_COFFEE,
                    "mua cafe"
            );
        }

        if (lowerText.contains("xăng")) {
            return new ExpenseParseResult(
                    new BigDecimal("80000"),
                    CategoryType.TRANSPORT,
                    "đổ xăng"
            );
        }

        return new ExpenseParseResult(
                new BigDecimal("0"),
                CategoryType.OTHER,
                text
        );
    }
}