package com.example.expensetracker.controller;

import com.example.expensetracker.controller.base.ControllerSupport;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.logging.audit.AuditDto;
import com.example.expensetracker.logging.audit.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "audit.tag.name", description = "audit.tag.desc")
@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController implements ControllerSupport {

    private final MessageSource messageSource;
    private final AuditService auditService;

    public AuditController(MessageSource messageSource, AuditService auditService) {
        this.messageSource = messageSource;
        this.auditService = auditService;
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }

    @GetMapping
    @Operation(
            summary = "audit.get.all.audit.sum",
            description = "audit.get.all.audit.desc")
    public ResponseEntity<ApiResponse<Page<AuditDto>>> getAllAudit(
            @PageableDefault(sort = "timeStamp", direction = Sort.Direction.DESC) @Parameter(hidden = true) Pageable pageable,
            HttpServletRequest request) {
        Page<AuditDto> logs = auditService.getAll(pageable);
        return ResponseEntity.ok(ApiResponseFactory.success(logs, msg("audit.controller.get.all"), request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "audit.get.by.admin.sum",
            description = "audit.get.by.admin.desc")
    public ResponseEntity<ApiResponse<Page<AuditDto>>> getByAdmin(
            @PathVariable Long id,
            @PageableDefault(sort = "timeStamp", direction = Sort.Direction.DESC) @Parameter(hidden = true) Pageable pageable,
            HttpServletRequest request) {
        Page<AuditDto> logs = auditService.getByAdmin(id, pageable);
        return ResponseEntity.ok(ApiResponseFactory.success(logs, msg("audit.controller.get.by.admin"), request));
    }
}

