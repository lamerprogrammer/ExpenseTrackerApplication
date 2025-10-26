package com.example.expensetracker.controller;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.dto.DateRangeDto;
import com.example.expensetracker.dto.ExpensesReportDto;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final MessageSource messageSource;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService, MessageSource messageSource, UserService userService) {
        this.expenseService = expenseService;
        this.messageSource = messageSource;
        this.userService = userService;
    }

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<ExpensesReportDto>> report(
            @Valid DateRangeDto range,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        var dto = expenseService.getReport(userDetails, range.getFrom(), range.getTo());
        return ResponseEntity.ok(ApiResponseFactory.success(dto, msg("expense.controller.report.ok"), request));
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotal(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        var total = userService.getTotalExpenses(userDetails.getDomainUser().getId());
        return ResponseEntity.ok(ApiResponseFactory.success(total, msg("expense.controller.total.ok"), request));
    }


    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
