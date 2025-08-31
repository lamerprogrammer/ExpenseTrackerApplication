package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class ApiResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    @JsonCreator
    public ApiResponse(@JsonProperty("timestamp") Instant timestamp,
                       @JsonProperty("status") int status,
                       @JsonProperty("error") String error,
                       @JsonProperty("message") String message,
                       @JsonProperty("path") String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
