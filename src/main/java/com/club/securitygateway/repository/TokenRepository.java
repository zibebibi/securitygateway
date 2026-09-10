package com.club.securitygateway.repository;

import com.club.securitygateway.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {

}