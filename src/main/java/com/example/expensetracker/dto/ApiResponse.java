package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class ApiResponse<T> {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final T data;
    private final List<String> errors;

    @JsonCreator
    public ApiResponse(@JsonProperty("timestamp") Instant timestamp,
                       @JsonProperty("status") int status,
                       @JsonProperty("error") String error,
                       @JsonProperty("message") String message,
                       @JsonProperty("path") String path,
                       @JsonProperty("data") T data,
                       @JsonProperty("errors") List<String> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.data = data;
        this.errors = errors;
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

    public T getData() {
        return data;
    }

    public List<String> getErrors() {
        return errors;
    }
}
