package com.example.expensetracker.logging;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class LogService {

    private final AppLogRepository appLogRepository;

    public LogService(AppLogRepository appLogRepository) {
        this.appLogRepository = appLogRepository;
    }

    public void log(LogEntry entry) {
        AppLog log = AppLog.builder()
                .timestamp(entry.getTimestamp())
                .level(entry.getLevel())
                .logger(entry.getLogger())
                .message(entry.getMessage())
                .userEmail(entry.getUser())
                .endPoint(entry.getPath())
                .stackTrace(entry.getStackTrace())
                .build();
        appLogRepository.save(log);
    }

    public List<AppLog> findAll() {
        return StreamSupport.stream(appLogRepository.findAll().spliterator(), false).toList();
    }

    public List<AppLog> findByUserEmail(String email) {
        return appLogRepository.findByUserEmail(email);
    }
}
