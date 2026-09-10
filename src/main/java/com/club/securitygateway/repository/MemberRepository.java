package com.club.securitygateway.repository;

import com.club.securitygateway.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이름으로 회원 검색
    List<Member> findByNameContaining(String name);
}