package com.example.expensetracker.controller;

import com.example.expensetracker.controller.base.ControllerSupport;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.dto.RecurringTransactionDto;
import com.example.expensetracker.dto.RecurringTransactionRequestDto;
import com.example.expensetracker.service.RecurringTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "recurring.transaction.tag.name", description = "recurring.transaction.tag.desc")
@RestController
@RequestMapping("/api/recurring-transaction")
public class RecurringTransactionController implements ControllerSupport {

    private final RecurringTransactionService recurringTransactionService;
    private final MessageSource messageSource;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService,
                                          MessageSource messageSource) {
        this.recurringTransactionService = recurringTransactionService;
        this.messageSource = messageSource;
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }

    @GetMapping
    @Operation(
            summary = "recurring.transaction.get.all.sum",
            description = "recurring.transaction.get.all.desc")
    public ResponseEntity<ApiResponse<List<RecurringTransactionDto>>> getAll(
            @AuthenticationPrincipal UserDetails currentUser, HttpServletRequest request) {
        List<RecurringTransactionDto> list = recurringTransactionService.getUserRecurringTransactions(currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(list, msg("recurring.transaction.controller.get.all"),
                request));
    }

    @PostMapping("/create")
    @Operation(
            summary = "recurring.transaction.create.sum",
            description = "recurring.transaction.create.desc")
    public ResponseEntity<ApiResponse<RecurringTransactionDto>> create(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody RecurringTransactionRequestDto dto,
            HttpServletRequest request) {
        RecurringTransactionDto created = recurringTransactionService.createRecurringTransaction(currentUser, dto);
        return ResponseEntity.ok(ApiResponseFactory.success(created, msg("recurring.transaction.controller.create"),
                request));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(
            summary = "recurring.transaction.toggle.active.sum",
            description = "recurring.transaction.toggle.active.desc")
    public ResponseEntity<ApiResponse<RecurringTransactionDto>> toggleActive(@PathVariable Long id,
                                                                             HttpServletRequest request) {
        RecurringTransactionDto update = recurringTransactionService.toggleActive(id);
        return ResponseEntity.ok(ApiResponseFactory.success(update, msg("recurring.transaction.controller.toggle.active"),
                request));
    }
}

