package com.example.expensetracker.controller;

import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.logging.audit.AuditDto;
import com.example.expensetracker.logging.applog.AppLogService;
import com.example.expensetracker.logging.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final MessageSource messageSource;
    private final AppLogService appLogService;
    private final AuditService auditService;

    public AuditController(MessageSource messageSource, AppLogService appLogService, AuditService auditService) {
        this.messageSource = messageSource;
        this.appLogService = appLogService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditDto>>> getAllAudit(Pageable pageable,
                                                                   HttpServletRequest request) {
        Page<AuditDto> logs = auditService.getAll(pageable);
        return ResponseEntity.ok(ApiResponseFactory.success(logs, msg("get.all.audit"), request));
    }

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
