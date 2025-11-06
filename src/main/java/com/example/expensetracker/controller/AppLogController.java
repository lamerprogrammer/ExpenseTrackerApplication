package com.example.expensetracker.controller;


import com.example.expensetracker.controller.base.ControllerSupport;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.logging.applog.AppLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "app.log.tag.name", description = "app.log.tag.desc")
@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class AppLogController implements ControllerSupport {

    private final AppLogService appLogService;
    private final MessageSource messageSource;

    public AppLogController(AppLogService appLogService, MessageSource messageSource) {
        this.appLogService = appLogService;
        this.messageSource = messageSource;
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }

    @GetMapping
    @Operation(
            summary = "app.log.get.all.logs.sum",
            description = "app.log.get.all.logs.desc")
    public ResponseEntity<ApiResponse<Page<AppLogDto>>> getAllLogs(@Parameter(hidden = true) Pageable pageable,
                                                                   HttpServletRequest request) {
        Page<AppLogDto> logs = appLogService.findAll(pageable);
        return ResponseEntity.ok(ApiResponseFactory.success(logs, msg("app.log.controller.logs.get.all"), request));
    }

    @GetMapping("/user/{email}")
    @Operation(
            summary = "app.log.get.by.user.sum",
            description = "app.log.get.by.user.desc")
    public ResponseEntity<ApiResponse<Page<AppLogDto>>> getByUser(@PathVariable String email,
                                                                  @Parameter(hidden = true) Pageable pageable,
                                                                  HttpServletRequest request) {
        Page<AppLogDto> logs = appLogService.findByUserEmail(email, pageable);
        return ResponseEntity.ok(ApiResponseFactory.success(logs, msg("app.log.controller.logs.get.by.user"), request));
    }
}
