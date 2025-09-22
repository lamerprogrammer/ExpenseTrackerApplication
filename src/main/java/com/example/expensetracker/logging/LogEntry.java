package com.example.expensetracker.logging;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntry {
    private Instant timestamp;
    private AuditLevel level;
    private String logger;
    private String message;
    private String user;
    private String path;

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public AuditLevel getLevel() {
        return level;
    }

    public void setLevel(AuditLevel level) {
        this.level = level;
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public static LogEntityBuilder builder() {
        return new LogEntityBuilder();
    }

    public static class LogEntityBuilder {
        private Instant timestamp;
        private AuditLevel level;
        private String logger;
        private String message;
        private String user;
        private String path;

        public LogEntityBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public LogEntityBuilder level(AuditLevel level) {
            this.level = level;
            return this;
        }

        public LogEntityBuilder logger(String logger) {
            this.logger = logger;
            return this;
        }

        public LogEntityBuilder message(String message) {
            this.message = message;
            return this;
        }

        public LogEntityBuilder user(String user) {
            this.user = user;
            return this;
        }

        public LogEntityBuilder path(String path) {
            this.path = path;
            return this;
        }

        public LogEntry build() {
            LogEntry entry = new LogEntry();
            entry.setTimestamp(this.timestamp);
            entry.setLevel(this.level);
            entry.setLogger(this.logger);
            entry.setMessage(this.message);
            entry.setUser(this.user);
            entry.setPath(this.path);
            return entry;
        }
    }
}
