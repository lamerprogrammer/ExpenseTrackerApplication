package com.example.expensetracker.controller;

import com.example.expensetracker.controller.base.ControllerSupport;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.dto.DateRangeDto;
import com.example.expensetracker.dto.ExpensesReportDto;
import com.example.expensetracker.model.Month;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "expense.tag.name", description = "expense.tag.desc")
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController implements ControllerSupport {

    private final ExpenseService expenseService;
    private final MessageSource messageSource;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService, MessageSource messageSource, UserService userService) {
        this.expenseService = expenseService;
        this.messageSource = messageSource;
        this.userService = userService;
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }

    @GetMapping("/report")
    @Operation(
            summary = "expense.report.sum",
            description = "expense.report.desc")
    public ResponseEntity<ApiResponse<ExpensesReportDto>> report(
            @Valid DateRangeDto range,
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            HttpServletRequest request) {
        var dto = expenseService.getReport(currentUser, range.getFrom(), range.getTo());
        return ResponseEntity.ok(ApiResponseFactory.success(dto, msg("expense.controller.report.ok"), request));
    }

    @GetMapping("/stats/monthly")
    @Operation(
            summary = "expense.report.monthly.sum",
            description = "expense.report.monthly.desc")
    public ResponseEntity<ApiResponse<ExpensesReportDto>> reportMonthly(
            @RequestParam(name = "month") @NotNull Month month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            HttpServletRequest request) {
        ExpensesReportDto dto = expenseService.getReportMonthly(month, year, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(dto, msg("expense.controller.report.monthly"), request));
    }

    @GetMapping("/total")
    @Operation(
            summary = "expense.get.total.sum",
            description = "expense.get.total.desc")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotal(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            HttpServletRequest request) {
        var total = userService.getTotalExpenses(currentUser.getDomainUser().getId());
        return ResponseEntity.ok(ApiResponseFactory.success(total, msg("expense.controller.total.ok"), request));
    }
}

