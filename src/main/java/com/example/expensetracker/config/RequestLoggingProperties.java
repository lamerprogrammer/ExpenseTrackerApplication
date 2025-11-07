package com.example.expensetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.logging.requests")
public record RequestLoggingProperties(boolean enabled) {
}
