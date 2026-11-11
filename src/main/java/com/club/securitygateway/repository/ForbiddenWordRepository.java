package com.club.securitygateway.repository;

import com.club.securitygateway.entity.ForbiddenWord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ForbiddenWordRepository extends JpaRepository<ForbiddenWord, Long> {
    List<ForbiddenWord> findAllByOrderByIdAsc();
    List<ForbiddenWord> findByActiveTrueOrderByIdAsc();
    Optional<ForbiddenWord> findByWordIgnoreCase(String word);
}
