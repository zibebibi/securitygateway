package com.club.securitygateway.controller;

import com.club.securitygateway.entity.Member;
import com.club.securitygateway.service.MemberService;
import com.club.securitygateway.service.PrivacyDetector;
import com.club.securitygateway.service.QuestionAnalyzer;
import com.club.securitygateway.service.RiskAnalyzer;
import com.club.securitygateway.service.TokenService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QuestionController {

    private final QuestionAnalyzer questionAnalyzer;
    private final RiskAnalyzer riskAnalyzer;
    private final PrivacyDetector privacyDetector;
    private final TokenService tokenService;
    private final MemberService memberService;

    public QuestionController(
            QuestionAnalyzer questionAnalyzer,
            RiskAnalyzer riskAnalyzer,
            PrivacyDetector privacyDetector,
            TokenService tokenService,
            MemberService memberService) {

        this.questionAnalyzer = questionAnalyzer;
        this.riskAnalyzer = riskAnalyzer;
        this.privacyDetector = privacyDetector;
        this.tokenService = tokenService;
        this.memberService = memberService;
    }

    @PostMapping("/question")
    public Object receiveQuestion(
            @RequestBody Map<String, String> request) {

        String question = request.get("question");

        // 1. 질문 확인
        if (question == null || question.trim().isEmpty()) {
            return createError("질문을 입력해주세요.");
        }

        // 2. 요청 유형 분석
        String requestType =
                questionAnalyzer.analyze(question);

        // 3. 회원 이름 추출
        String memberName =
                questionAnalyzer.extractMemberName(question);

        if (memberName == null) {
            return createError(
                    "질문에서 회원 이름을 찾을 수 없습니다."
            );
        }

        // 4. 회원 조회
        List<Member> members =
                memberService.searchByName(memberName);

        if (members.isEmpty()) {
            return createError(
                    "해당 회원이 존재하지 않습니다."
            );
        }

        Member member = members.get(0);

        // 5. 요청 자체의 위험도 분석
        String requestRisk =
                riskAnalyzer.analyze(requestType);

        // 6. 보안 정책 결정
        String securityAction =
                riskAnalyzer.getPolicy(requestRisk);


        /*
         * 7. HIGH 위험 요청 차단
         */

        if ("BLOCK".equals(securityAction)) {

            Map<String, Object> blockedResponse =
                    new HashMap<>();

            blockedResponse.put(
                    "blocked",
                    true
            );

            blockedResponse.put(
                    "securityAction",
                    "BLOCK"
            );

            blockedResponse.put(
                    "requestedType",
                    requestType
            );

            blockedResponse.put(
                    "requestRisk",
                    requestRisk
            );

            blockedResponse.put(
                    "error",
                    "보안 정책에 의해 해당 정보 요청이 차단되었습니다."
            );

            return blockedResponse;
        }


        /*
         * 8. 회원 개인정보 탐지
         */

        /*
         * 8. 요청한 개인정보만 탐지
         */

        String targetValue = null;

        if ("RRN".equals(requestType)) {

            targetValue = member.getRrn();

        }

        else if ("PHONE".equals(requestType)) {

            targetValue = member.getPhone();

        }

        else if ("EMAIL".equals(requestType)) {

            targetValue = member.getEmail();

        }

        else if ("MEMBER_INFO".equals(requestType)) {

            targetValue =
                    member.getRrn() + " "
                            + member.getPhone() + " "
                            + member.getEmail();
        }


        List<String> detected =
                privacyDetector.detect(targetValue);

        List<Map<String, String>> privacyResults =
                new ArrayList<>();

        for (String type : detected) {

            Map<String, String> result =
                    new HashMap<>();

            result.put(
                    "type",
                    type
            );

            result.put(
                    "risk",
                    riskAnalyzer.analyze(type)
            );

            privacyResults.add(result);
        }


        /*
         * 9. 개인정보 보호
         */

        String protectedRRN =
                tokenService.tokenizeRRN(
                        member.getRrn()
                );

        String protectedPhone =
                tokenService.tokenizePhone(
                        member.getPhone()
                );

        String protectedEmail =
                maskEmail(member.getEmail());


        /*
         * 10. 응답 생성
         */

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "name",
                member.getName()
        );

        response.put(
                "requestedType",
                requestType
        );

        response.put(
                "requestRisk",
                requestRisk
        );

        response.put(
                "securityAction",
                securityAction
        );

        response.put(
                "detectedPrivacy",
                privacyResults
        );


        /*
         * 11. 요청한 정보만 반환
         */

        if ("PHONE".equals(requestType)) {

            response.put(
                    "phone",
                    protectedPhone
            );
        }

        else if ("EMAIL".equals(requestType)) {

            response.put(
                    "email",
                    protectedEmail
            );
        }

        else if ("RRN".equals(requestType)) {

            response.put(
                    "rrn",
                    protectedRRN
            );
        }

        else if ("MEMBER_INFO".equals(requestType)) {

            response.put(
                    "rrn",
                    protectedRRN
            );

            response.put(
                    "phone",
                    protectedPhone
            );

            response.put(
                    "email",
                    protectedEmail
            );

            response.put(
                    "joinDate",
                    member.getJoinDate()
            );
        }

        return response;
    }


    /*
     * 이메일 마스킹
     */

    private String maskEmail(String email) {

        if (email == null ||
                !email.contains("@")) {

            return email;
        }

        String[] parts =
                email.split("@");

        String id = parts[0];
        String domain = parts[1];

        String visible;

        if (id.length() <= 1) {

            visible = id;

        } else if (id.length() <= 4) {

            visible =
                    id.substring(0, 1);

        } else {

            visible =
                    id.substring(0, 4);
        }

        return visible +
                "***@" +
                domain;
    }


    /*
     * 오류 응답
     */

    private Map<String, String> createError(
            String message) {

        Map<String, String> error =
                new HashMap<>();

        error.put(
                "error",
                message
        );

        return error;
    }
}