package com.club.securitygateway.controller;

import com.club.securitygateway.entity.GuardrailLog;
import com.club.securitygateway.repository.GuardrailLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guardrail-logs")
public class GuardrailLogController {

    private final GuardrailLogRepository repository;

    public GuardrailLogController(
            GuardrailLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<GuardrailLog> getLogs() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
}