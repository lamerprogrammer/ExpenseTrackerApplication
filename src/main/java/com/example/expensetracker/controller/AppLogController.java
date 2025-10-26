package com.example.expensetracker.controller;


import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.logging.applog.AppLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class AppLogController {

    private final AppLogService appLogService;
    private final MessageSource messageSource;

    public AppLogController(AppLogService appLogService, MessageSource messageSource) {
        this.appLogService = appLogService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AppLogDto>>> getAllLogs(Pageable pageable,
                                                                   HttpServletRequest request) {
        Page<AppLogDto> logs = appLogService.findAll(pageable);
        return ResponseEntity.ok(ApiResponseFactory.success(logs, msg("app.log.controller.logs.get.all"), request));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<ApiResponse<Page<AppLogDto>>> getByUser(@PathVariable String email,
                                                                  Pageable pageable,
                                                                  HttpServletRequest request) {
        Page<AppLogDto> logs = appLogService.findByUserEmail(email, pageable);
        return ResponseEntity.ok(ApiResponseFactory.success(logs, msg("app.log.controller.logs.get.by.user"), request));
    }

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
