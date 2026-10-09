package org.example.financemanagement.voice;

import org.example.financemanagement.ai.AIService;
import org.example.financemanagement.ai.ExpenseParseResult;
import org.example.financemanagement.expense.ExpenseService;
import org.example.financemanagement.expense.dto.ExpenseResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
public class VoiceController {

    private final VoiceService voiceService;
    private final AIService aiService;
    private final ExpenseService expenseService;

    public VoiceController(
            VoiceService voiceService,
            AIService aiService,
            ExpenseService expenseService
    ) {
        this.voiceService = voiceService;
        this.aiService = aiService;
        this.expenseService = expenseService;
    }

    @PostMapping("/{userId}/voice-expense")
    public ExpenseResponse createVoiceExpense(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        String text = voiceService.convertSpeechToText(file);
        ExpenseParseResult result = aiService.parseExpense(text);

        return expenseService.createVoiceExpense(
                userId,
                result.getCategoryType(),
                result.getAmount(),
                result.getDescription()
        );
    }

    @PostMapping("/voice/transcribe")
    public String transcribe(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        return voiceService.convertSpeechToText(file);
    }
}