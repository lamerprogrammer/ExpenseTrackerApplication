package com.example.expensetracker.controller;

import com.example.expensetracker.logging.AppLog;
import com.example.expensetracker.logging.LogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public List<AppLog> getAll() {
        return logService.findAll();
    }

    @GetMapping
    public List<AppLog> getByUser(@PathVariable String email) {
        return logService.findByUserEmail(email);
    }
}
