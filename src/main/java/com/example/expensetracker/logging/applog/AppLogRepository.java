package com.example.expensetracker.logging.applog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AppLogRepository extends ElasticsearchRepository<AppLog, String> {
    Page<AppLog> findByUserEmail(String userEmail, Pageable pageable);
}
