package com.club.securitygateway.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "forbidden_words")
public class ForbiddenWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String word;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 20)
    private String policy;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean defaultWord = false;

    public ForbiddenWord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPolicy() { return policy; }
    public void setPolicy(String policy) { this.policy = policy; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isDefaultWord() { return defaultWord; }
    public void setDefaultWord(boolean defaultWord) { this.defaultWord = defaultWord; }
}
