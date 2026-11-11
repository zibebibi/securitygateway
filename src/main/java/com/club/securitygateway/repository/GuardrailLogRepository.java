package com.club.securitygateway.repository;

import com.club.securitygateway.entity.GuardrailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuardrailLogRepository
        extends JpaRepository<GuardrailLog, Long> {

    List<GuardrailLog> findAllByOrderByCreatedAtDesc();
}