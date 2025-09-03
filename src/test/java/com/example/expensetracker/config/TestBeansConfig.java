package com.example.expensetracker.config;

import com.example.expensetracker.logging.AppLog;
import com.example.expensetracker.logging.AppLogRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@TestConfiguration
public class TestBeansConfig {

    @Bean
    @Primary
    public AppLogRepository appLogRepository() {
        AppLogRepository mockRepository = mock(AppLogRepository.class);
        List<AppLog> fakeStorage = new ArrayList<>();

        when(mockRepository.save(any(AppLog.class))).thenAnswer(invocation -> {
            AppLog log = invocation.getArgument(0);
            fakeStorage.add(log);
            return log;
        });
        when(mockRepository.findByUserEmail(anyString())).thenAnswer(invocation ->
            fakeStorage.stream()
                    .filter(l -> l.getUserEmail() != null &&
                            l.getUserEmail().equals(invocation.getArgument(0))).toList());
        when(mockRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(fakeStorage));
        return mockRepository;
    }
}
