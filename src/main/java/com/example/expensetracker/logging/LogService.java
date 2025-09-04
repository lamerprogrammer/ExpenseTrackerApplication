package com.example.expensetracker.logging;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class LogService {

    private final AppLogRepository appLogRepository;

    public LogService(AppLogRepository appLogRepository) {
        this.appLogRepository = appLogRepository;
    }

    public void log(String level, String message, String userEmail, String endPoint) {
        AppLog log = new AppLog();
        log.setTimestamp(Instant.now());
        log.setLevel(level);
        log.setMessage(message);
        log.setUserEmail(userEmail);
        log.setEndPoint(endPoint);

        appLogRepository.save(log);
    }

    public void log(String json) {
        AppLog log = new AppLog();
        log.setTimestamp(Instant.now());
        log.setLevel(json);
        log.setMessage(json);
        log.setUserEmail(json);
        log.setEndPoint(json);

        appLogRepository.save(log);
    }

    public List<AppLog> findAll() {
        return StreamSupport.stream(appLogRepository.findAll().spliterator(), false).toList();
    }

    public List<AppLog> findByUserEmail(String email) {
        return appLogRepository.findByUserEmail(email);
    }
}
