package com.club.securitygateway.controller;

import com.club.securitygateway.entity.AiPolicySetting;
import com.club.securitygateway.repository.AiPolicySettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/policies")
public class AiPolicyController {

    private final AiPolicySettingRepository repository;

    private static final Set<String> ALLOWED_POLICIES =
            Set.of("ALLOW", "GUIDE", "BLOCK");

    public AiPolicyController(
            AiPolicySettingRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AiPolicySetting> getPolicies() {
        return repository.findAll();
    }

    @PutMapping
    public List<AiPolicySetting> updatePolicies(
            @RequestBody List<Map<String, String>> requests) {

        for (Map<String, String> request : requests) {
            String category = request.get("category");
            String policy = request.get("policy");

            if (category == null || policy == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "category와 policy가 필요합니다."
                );
            }

            policy = policy.toUpperCase();

            if (!ALLOWED_POLICIES.contains(policy)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "정책은 ALLOW, GUIDE, BLOCK 중 하나여야 합니다."
                );
            }

            AiPolicySetting setting = repository
                    .findByCategory(category)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "등록되지 않은 정책 분류입니다: " + category
                    ));

            setting.setPolicy(policy);
            repository.save(setting);
        }

        return repository.findAll();
    }
}