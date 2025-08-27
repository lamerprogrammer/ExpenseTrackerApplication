package com.example.expensetracker.logging;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;


@Document(indexName = "app-logs")
public class AppLog {

    @Id
    private String id;
    private Instant timestamp;
    private String level;
    private String message;
    private String userEmail;
    private String endPoint;

    public AppLog() {
    }

    public AppLog(String id, Instant timestamp, String level, String message, String userEmail, String endPoint) {
        this.id = id;
        this.timestamp = timestamp;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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

    public static class AppLogBuilder {
        private String id;
        private Instant timestamp;
        private String level;
        private String message;
        private String userEmail;
        private String endPoint;

        public AppLogBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AppLogBuilder level(String level) {
            this.level = level;
            return this;
        }

        public AppLogBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
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

        public AppLog build() {
            return new AppLog(id, timestamp, level, message, userEmail, endPoint);
        }
    }
}
