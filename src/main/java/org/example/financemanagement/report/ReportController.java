package org.example.financemanagement.report;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/users")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{userId}/reports/total")
    public BigDecimal getTotalExpense(
            @PathVariable Long userId
    ) {
        return reportService.getTotalExpense(userId);
    }

    @GetMapping("/{userId}/reports/monthly")
    public BigDecimal getMonthlyExpense(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return reportService.getMonthlyExpense(userId, year, month);
    }
}