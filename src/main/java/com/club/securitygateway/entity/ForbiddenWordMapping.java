
package com.club.securitygateway.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "forbidden_word_mappings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_forbidden_word_mapping",
                        columnNames = {"forbidden_word_id", "mapped_word"}
                )
        }
)
public class ForbiddenWordMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "forbidden_word_id", nullable = false)
    private ForbiddenWord forbiddenWord;

    @Column(name = "mapped_word", nullable = false, length = 120)
    private String mappedWord;

    @Column(nullable = false)
    private boolean active = true;

    public ForbiddenWordMapping() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ForbiddenWord getForbiddenWord() {
        return forbiddenWord;
    }

    public void setForbiddenWord(ForbiddenWord forbiddenWord) {
        this.forbiddenWord = forbiddenWord;
    }

    public String getMappedWord() {
        return mappedWord;
    }

    public void setMappedWord(String mappedWord) {
        this.mappedWord = mappedWord;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
