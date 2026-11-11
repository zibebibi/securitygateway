package com.club.securitygateway.service;

import com.club.securitygateway.entity.Member;
import com.club.securitygateway.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

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

        /*
         * 1. 주민등록번호 탐지
         * 예:
         * 123456-1234567
         * 1234561234567
         */
        if (Pattern.compile(
                "\\d{6}-?[1-4]\\d{6}"
        ).matcher(question).find()) {
            return "RRN";
        }

        /*
         * 2. 전화번호 탐지
         * 예:
         * 010-1234-5678
         * 01012345678
         */
        if (Pattern.compile(
                "01[0-9]-?[0-9]{3,4}-?[0-9]{4}"
        ).matcher(question).find()) {
            return "PHONE";
        }

        /*
         * 3. 이메일 탐지
         * 예:
         * test@example.com
         */
        if (Pattern.compile(
                "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        ).matcher(question).find()) {
            return "EMAIL";
        }

        /*
         * 4. 개인정보 관련 키워드
         */
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

        // 이름 + 주민등록번호 형태
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern
                        .compile("([가-힣]{2,4})\\s*\\d{6}-?[1-4]\\d{6}")
                        .matcher(question);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // DB에 이미 존재하는 회원 이름 검색
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            if (question.contains(member.getName())) {
                return member.getName();
            }
        }

        return null;
    }
}