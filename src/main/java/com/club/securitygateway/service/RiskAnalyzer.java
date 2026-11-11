package com.club.securitygateway.service;

import org.springframework.stereotype.Service;

@Service
public class RiskAnalyzer {

    public String analyze(String type) {

        if ("RRN".equals(type)) {
            return "HIGH";
        }

        if ("PHONE".equals(type)) {
            return "MEDIUM";
        }

        if ("EMAIL".equals(type)) {
            return "MEDIUM";
        }

        if ("MEMBER_INFO".equals(type)) {
            return "HIGH";
        }

        return "LOW";
    }

    public String getPolicy(String risk) {

        if ("HIGH".equals(risk)) {
            return "PROTECT";
        }

        if ("MEDIUM".equals(risk)) {
            return "PROTECT";
        }

        return "ALLOW";
    }
}