package com.club.securitygateway.service;

import com.club.securitygateway.entity.Member;
import com.club.securitygateway.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionAnalyzer {

    private final MemberRepository memberRepository;

    public QuestionAnalyzer(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 질문 유형 분석
    public String analyze(String question) {

        if (question == null || question.trim().isEmpty()) {
            return "EMPTY";
        }

        if (question.contains("주민등록번호")
                || question.contains("주민번호")) {
            return "RRN";
        }

        if (question.contains("전화번호")
                || question.contains("전화")
                || question.contains("연락처")) {
            return "PHONE";
        }

        if (question.contains("이메일")
                || question.contains("메일")) {
            return "EMAIL";
        }

        if (question.contains("회원정보")
                || question.contains("회원 정보")
                || question.contains("정보")) {
            return "MEMBER_INFO";
        }

        return "UNKNOWN";
    }

    // 질문에서 DB에 존재하는 회원 이름 찾기
    public String extractMemberName(String question) {

        if (question == null || question.trim().isEmpty()) {
            return null;
        }

        List<Member> members = memberRepository.findAll();

        for (Member member : members) {

            if (question.contains(member.getName())) {
                return member.getName();
            }
        }

        return null;
    }
}