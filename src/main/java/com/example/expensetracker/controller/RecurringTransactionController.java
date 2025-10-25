package com.example.expensetracker.controller;

import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.dto.RecurringTransactionDto;
import com.example.expensetracker.dto.RecurringTransactionRequestDto;
import com.example.expensetracker.service.RecurringTransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-transaction")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;
    private final MessageSource messageSource;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService, 
                                          MessageSource messageSource) {
        this.recurringTransactionService = recurringTransactionService;
        this.messageSource = messageSource;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringTransactionDto>>> getAll(
            @AuthenticationPrincipal UserDetails currentUser, HttpServletRequest request) {
        List<RecurringTransactionDto> list = recurringTransactionService.getUserRecurringTransactions(currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(list, msg("recurring.transaction.get.all"), request));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RecurringTransactionDto>> create(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody RecurringTransactionRequestDto dto,
            HttpServletRequest request) {
        RecurringTransactionDto created = recurringTransactionService.createRecurringTransaction(currentUser, dto);
        return ResponseEntity.ok(ApiResponseFactory.success(created, msg("recurring.transaction.create"), request));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<RecurringTransactionDto>> toggleActive(@PathVariable Long id,
            HttpServletRequest request) {
        RecurringTransactionDto update = recurringTransactionService.toggleActive(id);
        return ResponseEntity.ok(ApiResponseFactory.success(update, msg("recurring.transaction.toggle.active"), request));
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
