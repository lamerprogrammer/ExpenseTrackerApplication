package com.example.expensetracker.logging.applog;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Document(indexName = "app-logs")
public class AppLog {

    @Id
    private String id;
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant timestamp;
    private AppLogLevel level;
    private String logger;
    private String errorType;
    private String message;
    private String userEmail;
    private String endPoint;

    public AppLog() {
    }

    public AppLog(String id, 
                  Instant timestamp, 
                  AppLogLevel level, 
                  String logger,
                  String errorType,
                  String message, 
                  String userEmail, 
                  String endPoint) {
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public AppLogLevel getLevel() {
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

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setLevel(AppLogLevel level) {
        this.level = level;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public static AppLog from(AppLogDto dto) {
        return new AppLog(
                dto.getId(),
                dto.getTimestamp(),
                dto.getLevel(),
                dto.getLogger(),
                dto.getErrorType(),
                dto.getMessage(),
                dto.getUserEmail(),
                dto.getEndPoint()
        );
    }
}
