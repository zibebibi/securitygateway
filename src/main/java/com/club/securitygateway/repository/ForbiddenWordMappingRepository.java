
package com.club.securitygateway.repository;

import com.club.securitygateway.entity.ForbiddenWordMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForbiddenWordMappingRepository
        extends JpaRepository<ForbiddenWordMapping, Long> {

    List<ForbiddenWordMapping> findByActiveTrue();
}
