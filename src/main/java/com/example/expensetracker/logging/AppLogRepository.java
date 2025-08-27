package com.example.expensetracker.logging;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface AppLogRepository extends ElasticsearchRepository<AppLog, String> {
    List<AppLog> findByUserEmail(String userEmail);
}
