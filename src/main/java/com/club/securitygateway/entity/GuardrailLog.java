package com.club.securitygateway.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "guardrail_logs")
public class GuardrailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String question;

    @Column(length = 100)
    private String requestedType;

    @Column(length = 20)
    private String risk;

    @Column(nullable = false, length = 20)
    private String securityAction;

    @Column(nullable = false)
    private boolean blocked;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GuardrailLog() {
    }

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getRequestedType() {
        return requestedType;
    }

    public void setRequestedType(String requestedType) {
        this.requestedType = requestedType;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public String getSecurityAction() {
        return securityAction;
    }

    public void setSecurityAction(String securityAction) {
        this.securityAction = securityAction;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}