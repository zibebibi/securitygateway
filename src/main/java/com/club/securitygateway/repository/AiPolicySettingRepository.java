package com.club.securitygateway.repository;

import com.club.securitygateway.entity.AiPolicySetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiPolicySettingRepository
        extends JpaRepository<AiPolicySetting, Long> {

    Optional<AiPolicySetting> findByCategory(String category);

    List<AiPolicySetting> findByActiveTrue();
}