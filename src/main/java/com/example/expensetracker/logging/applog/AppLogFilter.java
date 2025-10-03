package com.example.expensetracker.logging.applog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class AppLogFilter extends OncePerRequestFilter {
    
    private final AppLogService appLogService;

    public AppLogFilter(AppLogService appLogService) {
        this.appLogService = appLogService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";

            AppLogDto logDto = getAppLogDto(request, response, duration, userEmail);

            appLogService.log(logDto);
        }
    }

    private static AppLogDto getAppLogDto(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          long duration, 
                                          String userEmail) {
        return new AppLogDto(
                null,
                Instant.now(),
                AppLogLevel.INFO,
                "HTTP",
                null,
                String.format("%s %s -> %d (%d ms)",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        duration),
                userEmail,
                request.getRequestURI()
        );
    }
}
