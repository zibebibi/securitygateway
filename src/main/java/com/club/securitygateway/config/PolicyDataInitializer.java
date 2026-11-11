package com.club.securitygateway.config;

import com.club.securitygateway.entity.AiPolicySetting;
import com.club.securitygateway.repository.AiPolicySettingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PolicyDataInitializer {

    @Bean
    CommandLineRunner initializePolicies(
            AiPolicySettingRepository repository) {

        return args -> {
            addIfMissing(
                    repository,
                    "일반 학습 질문",
                    "수업, 개념 설명, 문제 풀이 등 학습에 도움이 되는 질문",
                    "ALLOW"
            );

            addIfMissing(
                    repository,
                    "학습 방해 요소",
                    "학습 목적과 관계없는 영상, 게임 추천 등의 요청",
                    "GUIDE"
            );

            addIfMissing(
                    repository,
                    "학습 부정행위",
                    "시험 답만 요구하거나 과제를 대신 해달라는 요청",
                    "GUIDE"
            );

            addIfMissing(
                    repository,
                    "개인정보",
                    "개인정보가 포함된 요청",
                    "GUIDE"
            );

            addIfMissing(
                    repository,
                    "폭력·위험 요청",
                    "위험한 행동을 돕는 요청",
                    "BLOCK"
            );

            addIfMissing(
                    repository,
                    "프롬프트 인젝션",
                    "보안 검사 우회 또는 시스템 지시 변경 시도",
                    "BLOCK"
            );
        };
    }

    private void addIfMissing(
            AiPolicySettingRepository repository,
            String category,
            String description,
            String policy) {

        if (repository.findByCategory(category).isEmpty()) {
            AiPolicySetting setting = new AiPolicySetting();
            setting.setCategory(category);
            setting.setDescription(description);
            setting.setPolicy(policy);
            setting.setActive(true);

            repository.save(setting);
        }
    }
}