package com.example.expensetracker.logging.applog;

import java.time.Instant;
import java.util.Objects;

public class AppLogDto {

    private String id;
    private Instant timestamp;
    private AppLogLevel level;
    private String logger;
    private String errorType;
    private String message;
    private String userEmail;
    private String endPoint;
    
    
    public AppLogDto() {}

    public AppLogDto(String id, Instant timestamp, AppLogLevel level, String logger, String errorType, 
                     String message, String userEmail, String endPoint) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.logger = logger;
        this.errorType = errorType;
        this.message = message;
        this.userEmail = userEmail;
        this.endPoint = endPoint;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public AppLogLevel getLevel() {
        return level;
    }

    public void setLevel(AppLogLevel level) {
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public static AppLogDto from(AppLog entity) {
        return new AppLogDto(
                entity.getId(),
                entity.getTimestamp(),
                entity.getLevel(),
                entity.getLogger(),
                entity.getErrorType(),
                entity.getMessage(),
                entity.getUserEmail(),
                entity.getEndPoint()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppLogDto logDto = (AppLogDto) o;
        return Objects.equals(id, logDto.id) && 
                Objects.equals(timestamp, logDto.timestamp) && 
                level == logDto.level && 
                Objects.equals(logger, logDto.logger) &&
                Objects.equals(errorType, logDto.errorType) &&
                Objects.equals(message, logDto.message) && 
                Objects.equals(userEmail, logDto.userEmail) && 
                Objects.equals(endPoint, logDto.endPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, level, logger, errorType, message, userEmail, endPoint);
    }
}