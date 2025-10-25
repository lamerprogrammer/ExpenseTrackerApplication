package com.example.expensetracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Класс создан для работы фильтрации доступа через @PreAuthorize("hasRole('ADMIN')") и аналогичных. 
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
