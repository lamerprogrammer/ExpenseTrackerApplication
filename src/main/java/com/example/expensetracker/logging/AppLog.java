package com.example.expensetracker.logging;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;
import java.util.Objects;


@Document(indexName = "app-logs")
public class AppLog {

    @Id
    private String id;
    private Instant timestamp;
    private String level;
    private String logger;
    private String message;
    private String userEmail;
    private String endPoint;
    private String stackTrace;

    public AppLog() {
    }

    public AppLog(String id,
                  Instant timestamp,
                  String level,
                  String logger,
                  String message,
                  String userEmail,
                  String endPoint,
                  String stackTrace) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.logger = logger;
        this.message = message;
        this.userEmail = userEmail;
        this.endPoint = endPoint;
        this.stackTrace = stackTrace;
    }

    public String getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getLogger() {
        return logger;
    }

    public String getMessage() {
        return message;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppLog log = (AppLog) o;
        return Objects.equals(id, log.id) &&
                Objects.equals(timestamp, log.timestamp) &&
                Objects.equals(level, log.level) &&
                Objects.equals(logger, log.logger) &&
                Objects.equals(message, log.message) &&
                Objects.equals(userEmail, log.userEmail) &&
                Objects.equals(endPoint, log.endPoint) &&
                Objects.equals(stackTrace, log.stackTrace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, level, logger, message, userEmail, endPoint, stackTrace);
    }

    public static AppLogBuilder builder() {
        return new AppLogBuilder();
    }

    public static class AppLogBuilder {
        private String id;
        private Instant timestamp;
        private String level;
        private String logger;
        private String message;
        private String userEmail;
        private String endPoint;
        private String stackTrace;

        public AppLogBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AppLogBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AppLogBuilder level(String level) {
            this.level = level;
            return this;
        }

        public AppLogBuilder logger(String logger) {
            this.logger = logger;
            return this;
        }

        public AppLogBuilder message(String message) {
            this.message = message;
            return this;
        }

        public AppLogBuilder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public AppLogBuilder endPoint(String endPoint) {
            this.endPoint = endPoint;
            return this;
        }

        public AppLogBuilder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public AppLog build() {
            return new AppLog(id, timestamp, level, logger, message, userEmail, endPoint, stackTrace);
        }
    }
}
