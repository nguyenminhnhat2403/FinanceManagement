//package org.example.financemanagement.ai;
//
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/ai")
//public class AIController {
//
//    private final AIService aiService;
//
//    public AIController(AIService aiService) {
//        this.aiService = aiService;
//    }
//
//    @PostMapping("/parse-expense")
//    public ExpenseParseResult parseExpense(@RequestBody String text) {
//        return aiService.parseExpense(text);
//    }
//}