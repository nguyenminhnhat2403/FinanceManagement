//package org.example.financemanagement.ai;
//
//import tools.jackson.databind.JsonNode;
//import tools.jackson.databind.ObjectMapper;
//import org.example.financemanagement.category.CategoryType;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class AIService {
//
//    @Value("${openai.api.key}")
//    private String apiKey;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public ExpenseParseResult parseExpense(String text) {
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        // 1. Header
//        HttpHeaders headers = new HttpHeaders();
//
//        headers.setBearerAuth(apiKey);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        String categories = Arrays.stream(CategoryType.values())
//                .map(Enum::name)
//                .collect(Collectors.joining(", "));
//
//        // 2. Prompt
//        String prompt = """
//        Bạn là hệ thống phân tích chi tiêu.
//
//        Hãy phân tích câu sau:
//        "%s"
//
//        categoryType chỉ được phép là một trong các giá trị:
//        %s
//
//        Chỉ trả về JSON theo đúng format:
//
//        {
//          "amount": 30000,
//          "categoryType": "FOOD",
//          "description": "cafe"
//        }
//
//        Không giải thích gì thêm.
//        """.formatted(text, categories);
//        // 3. Body gửi OpenAI
//        Map<String, Object> body = new HashMap<>();
//
//        body.put("model", "gpt-4.1-mini");
//        body.put("input", prompt);
//
//        // 4. Ghép body + headers
//        HttpEntity<Map<String, Object>> request =
//                new HttpEntity<>(body, headers);
//
//        // 5. Gửi request
//        ResponseEntity<String> response =
//                restTemplate.postForEntity(
//                        "https://api.openai.com/v1/responses",
//                        request,
//                        String.class
//                );
//
//        // 6. Lấy JSON OpenAI trả về
//        String responseBody = response.getBody();
//
//        try {
//
//            JsonNode root = objectMapper.readTree(responseBody);
//
//            // text OpenAI sinh ra nằm trong output
//            String aiText =
//                    root.get("output")
//                            .get(0)
//                            .get("content")
//                            .get(0)
//                            .get("text")
//                            .asText();
//
//            // aiText lúc này là:
//            // {"amount":30000,"categoryType":"FOOD_COFFEE","description":"cafe"}
//
//            JsonNode expenseJson = objectMapper.readTree(aiText);
//
//            BigDecimal amount =
//                    expenseJson.get("amount").decimalValue();
//
//            CategoryType categoryType =
//                    CategoryType.valueOf(
//                            expenseJson.get("categoryType").asText()
//                    );
//
//            String description =
//                    expenseJson.get("description").asText();
//
//            return new ExpenseParseResult(
//                    amount,
//                    categoryType,
//                    description
//            );
//
//        } catch (Exception e) {
//            throw new RuntimeException("Cannot parse AI response", e);
//        }
//    }
//}