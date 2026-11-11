package com.club.securitygateway.controller;

import com.club.securitygateway.entity.Member;
import com.club.securitygateway.entity.AiPolicySetting;
import com.club.securitygateway.entity.GuardrailLog;
import com.club.securitygateway.entity.ForbiddenWord;
import com.club.securitygateway.repository.AiPolicySettingRepository;
import com.club.securitygateway.repository.GuardrailLogRepository;
import com.club.securitygateway.repository.ForbiddenWordRepository;
import com.club.securitygateway.service.MemberService;
import com.club.securitygateway.service.OpenAiService;
import com.club.securitygateway.service.QuestionAnalyzer;
import com.club.securitygateway.service.RiskAnalyzer;
import com.club.securitygateway.service.PromptInjectionDetector;
import com.club.securitygateway.repository.ForbiddenWordMappingRepository;
import com.club.securitygateway.entity.ForbiddenWordMapping;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class GuardrailController {

    private final QuestionAnalyzer questionAnalyzer;
    private final RiskAnalyzer riskAnalyzer;
    private final OpenAiService openAiService;
    private final PromptInjectionDetector promptInjectionDetector;
    private final MemberService memberService;
    private final AiPolicySettingRepository policyRepository;
    private final GuardrailLogRepository logRepository;
    private final ForbiddenWordRepository forbiddenWordRepository;
    private final ForbiddenWordMappingRepository forbiddenWordMappingRepository;

    public GuardrailController(
            QuestionAnalyzer questionAnalyzer,
            RiskAnalyzer riskAnalyzer,
            OpenAiService openAiService,
            PromptInjectionDetector promptInjectionDetector,
            MemberService memberService,
            AiPolicySettingRepository policyRepository,
            GuardrailLogRepository logRepository,
            ForbiddenWordRepository forbiddenWordRepository,
            ForbiddenWordMappingRepository forbiddenWordMappingRepository) {

        this.questionAnalyzer = questionAnalyzer;
        this.riskAnalyzer = riskAnalyzer;
        this.openAiService = openAiService;
        this.promptInjectionDetector = promptInjectionDetector;
        this.memberService = memberService;
        this.policyRepository = policyRepository;
        this.logRepository = logRepository;
        this.forbiddenWordRepository = forbiddenWordRepository;
        this.forbiddenWordMappingRepository = forbiddenWordMappingRepository;
    }

    @PostMapping("/guardrail")
    public Map<String, Object> guardrail(
            @RequestBody Map<String, String> request) {

        String question = request.get("question");

        Map<String, Object> response = new HashMap<>();

        // =====================================================
        // 1. 질문 확인
        // =====================================================

        if (question == null || question.trim().isEmpty()) {

            response.put("error", "질문을 입력해주세요.");

            return response;
        }

        // =====================================================
        // 2. 프롬프트 인젝션 검사
        // =====================================================

        boolean promptInjection =
                promptInjectionDetector.detect(question);

        response.put(
                "promptInjectionDetected",
                promptInjection
        );

        if (promptInjection) {

            response.put(
                    "requestedType",
                    "PROMPT_INJECTION"
            );

            response.put("risk", "HIGH");
            response.put("securityAction", "BLOCK");
            response.put("blocked", true);

            response.put(
                    "message",
                    "프롬프트 인젝션이 탐지되어 해당 요청이 차단되었습니다."
            );
            saveGuardrailLog(
                    question,
                    "PROMPT_INJECTION",
                    "HIGH",
                    "BLOCK",
                    true
            );
            return response;
        }

        // =====================================================
        // 3. 질문 유형 분석
        // =====================================================

        String requestType =
                questionAnalyzer.analyze(question);

        response.put(
                "requestedType",
                requestType
        );

        // =====================================================
        // 4. 회원 저장 요청
        // =====================================================

        if (isSaveRequest(question)) {

            Member member =
                    createMemberFromQuestion(question);

            if (member == null) {

                response.put("saved", false);

                response.put(
                        "message",
                        "저장할 회원 정보를 찾을 수 없습니다."
                );

                return response;
            }

            // DB 저장
            Member savedMember =
                    memberService.save(member);

            // 주민번호 등의 개인정보를 보호
            String protectedQuestion =
                    protectPersonalInfo(question);

            response.put("saved", true);

            response.put(
                    "savedMemberId",
                    savedMember.getId()
            );

            response.put(
                    "message",
                    "회원 정보를 DB에 저장했습니다."
            );

            response.put(
                    "protectedQuestion",
                    protectedQuestion
            );

            // 보호된 질문만 OpenAI에 전달
            String answer =
                    openAiService.ask(protectedQuestion);

            response.put("blocked", false);
            response.put("answer", answer);

            return response;
        }

        // =====================================================
        // 5. 회원 조회 요청
        // =====================================================

        if (isSearchRequest(question)) {

            String name =
                    extractSearchName(question);

            if (name != null) {

                List<Member> members =
                        memberService.searchByName(name);

                if (members.isEmpty()) {

                    response.put("found", false);

                    response.put(
                            "message",
                            "해당 회원을 찾을 수 없습니다."
                    );

                    return response;
                }

                // 개인정보를 마스킹해서 반환
                response.put(
                        "members",
                        protectMembers(members)
                );

                response.put("found", true);

                response.put(
                        "message",
                        "회원 정보를 조회했습니다."
                );

                // 조회는 OpenAI를 호출하지 않음
                response.put("blocked", false);

                return response;
            }
        }

        // =====================================================
// 6. 위험도 분석
// =====================================================

        String risk = riskAnalyzer.analyze(requestType);
        response.put("risk", risk);

// =====================================================
// 7. DB 정책 조회 및 최종 판정
// =====================================================

        // 부모가 DB에 등록한 활성 표현을 질문마다 조회합니다.
        ForbiddenWord matchedWord = findMatchingForbiddenWord(question);
        String category = matchedWord != null
                ? matchedWord.getCategory()
                : detectPolicyCategory(question);
        String dbPolicy = matchedWord != null
                ? matchedWord.getPolicy()
                : getPolicyFromDatabase(category);

        if (dbPolicy != null) {
            dbPolicy = dbPolicy.trim().toUpperCase();
        }
        // 기존 위험도 분석 결과도 유지하며, 위험 판정은 부모 설정으로 완화하지 않습니다.
        String existingAction = riskAnalyzer.getPolicy(risk);

        if (existingAction != null) {
            existingAction = existingAction.trim().toUpperCase();
        }

        // 단어가 포함됐더라도 교육 목적의 개념 질문이면 단어 하나만으로 안내/차단하지 않습니다.
        // 단, 위험 행동의 방법을 묻거나 기존 위험 분석이 BLOCK이면 예외입니다.


        String action;

// 기존 보안 분석이 BLOCK이면 DB 설정으로 허용하지 않음
        if ("BLOCK".equalsIgnoreCase(existingAction)) {
            action = "BLOCK";
        } else if (dbPolicy == null) {
            // DB 정책이 없거나 비활성화된 경우 기존 판정을 유지
            action = existingAction;
        } else if ("PROTECT".equalsIgnoreCase(existingAction)
                && "ALLOW".equalsIgnoreCase(dbPolicy)) {
            // 기존 개인정보 보호 처리 유지
            action = "PROTECT";
        } else {
            // DB에 저장된 정책을 실제 판정에 반영
            action = dbPolicy;
        }

        response.put("policyCategory", category);
        response.put("securityAction", action);
        System.out.println("[DEBUG] question = " + question);
        System.out.println("[DEBUG] matchedWord = " +
                (matchedWord == null ? "null" : matchedWord.getWord()));
        System.out.println("[DEBUG] dbPolicy = " + dbPolicy);
        System.out.println("[DEBUG] securityAction = " + action);

// =====================================================
// 8. BLOCK
// =====================================================

        if ("BLOCK".equalsIgnoreCase(action)) {
            response.put("blocked", true);
            response.put(
                    "message",
                    "보안 정책에 의해 해당 요청이 차단되었습니다."
            );

            saveGuardrailLog(
                    question,
                    requestType,
                    risk,
                    action,
                    true
            );

            return response;
        }

// =====================================================
// 9. GUIDE
// =====================================================

        if ("GUIDE".equalsIgnoreCase(action)) {
            response.put("blocked", false);
            response.put(
                    "message",
                    "이 요청은 현재 학습 정책에 따라 안내가 필요합니다. "
                            + "학습 목표와 관련된 질문으로 바꿔서 다시 시도해주세요."
            );

            saveGuardrailLog(
                    question,
                    requestType,
                    risk,
                    action,
                    false
            );

            return response;
        }

// =====================================================
// 10. 개인정보 보호
// =====================================================

        String protectedQuestion = question;

        if ("PROTECT".equalsIgnoreCase(action)) {
            protectedQuestion = protectPersonalInfo(question);
        }

        response.put("protectedQuestion", protectedQuestion);

// =====================================================
// 11. 일반 질문 → OpenAI
// =====================================================

        String answer = openAiService.ask(protectedQuestion);

        response.put("blocked", false);
        response.put("answer", answer);

        saveGuardrailLog(
                question,
                requestType,
                risk,
                action,
                false
        );

        return response;
    }

    // =========================================================
    // 저장 요청인지 확인
    // =========================================================

    private boolean isSaveRequest(String question) {

        return question.contains("저장해줘")
                || question.contains("저장해")
                || question.contains("회원으로 등록")
                || question.contains("회원 등록")
                || question.contains("회원으로 저장");
    }

    // =========================================================
    // 조회 요청인지 확인
    // =========================================================

    private boolean isSearchRequest(String question) {

        return question.contains("조회")
                || question.contains("찾아줘")
                || question.contains("찾아")
                || question.contains("알려줘")
                || question.contains("알려");
    }

    // =========================================================
    // 조회할 이름 추출
    // =========================================================

    private String extractSearchName(String question) {

        // 예:
        // 홍길동 회원 정보 알려줘

        Matcher matcher =
                Pattern
                        .compile("([가-힣]{2,4})\\s*(회원|회원정보|정보)")
                        .matcher(question);

        if (matcher.find()) {

            return matcher.group(1);
        }

        // DB에 존재하는 회원 이름 검색
        List<Member> members =
                memberService.findAll();

        for (Member member : members) {

            if (question.contains(member.getName())) {

                return member.getName();
            }
        }

        return null;
    }

    // =========================================================
    // 질문에서 회원 정보 추출
    // =========================================================

    private Member createMemberFromQuestion(String question) {

        // 1. 질문에서 회원 이름 직접 추출
        String name = null;

        Matcher nameMatcher =
                Pattern.compile("([가-힣]{2,4})\\s*(?:회원|님)?\\s*(?:으로\\s*)?(?:저장|등록)")
                        .matcher(question);

        if (nameMatcher.find()) {
            name = nameMatcher.group(1);
        }

        // 기존 DB 회원 이름 검색도 보조적으로 사용
        if (name == null) {
            name = questionAnalyzer.extractMemberName(question);
        }

        // 이름을 찾지 못하면 저장하지 않음
        if (name == null) {
            return null;
        }

        Member member = new Member();

        member.setName(name);

        // 2. 주민등록번호 추출
        Matcher rrnMatcher =
                Pattern.compile("\\d{6}-?[1-4]\\d{6}")
                        .matcher(question);

        if (rrnMatcher.find()) {
            member.setRrn(rrnMatcher.group());
        }

        // 3. 전화번호 추출
        Matcher phoneMatcher =
                Pattern.compile("01[0-9]-?[0-9]{3,4}-?[0-9]{4}")
                        .matcher(question);

        if (phoneMatcher.find()) {
            member.setPhone(phoneMatcher.group());
        }

        // 4. 이메일 추출
        Matcher emailMatcher =
                Pattern.compile(
                        "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
                ).matcher(question);

        if (emailMatcher.find()) {
            member.setEmail(emailMatcher.group());
        }

        // 5. 가입일
        member.setJoinDate(LocalDate.now());

        return member;
    }

    // =========================================================
    // LLM에 전달하기 전에 개인정보 보호
    // =========================================================

    private String protectPersonalInfo(String question) {

        String result = question;

        // 주민등록번호
        result = result.replaceAll(
                "\\d{6}-?[1-4]\\d{6}",
                "[PROTECTED_RRN]"
        );

        // 전화번호
        result = result.replaceAll(
                "01[0-9]-?[0-9]{3,4}-?[0-9]{4}",
                "[PROTECTED_PHONE]"
        );

        // 이메일
        result = result.replaceAll(
                "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}",
                "[PROTECTED_EMAIL]"
        );

        return result;
    }

    // =========================================================
    // 조회 결과 개인정보 보호
    // =========================================================

    private List<Map<String, Object>> protectMembers(
            List<Member> members) {

        return members.stream()
                .map(member -> {

                    Map<String, Object> data =
                            new HashMap<>();

                    data.put(
                            "id",
                            member.getId()
                    );

                    data.put(
                            "name",
                            member.getName()
                    );

                    data.put(
                            "rrn",
                            maskRrn(member.getRrn())
                    );

                    data.put(
                            "phone",
                            maskPhone(member.getPhone())
                    );

                    data.put(
                            "email",
                            maskEmail(member.getEmail())
                    );

                    data.put(
                            "club",
                            member.getClub()
                    );

                    data.put(
                            "joinDate",
                            member.getJoinDate()
                    );

                    return data;
                })
                .toList();
    }

    // =========================================================
    // 주민번호 마스킹
    // =========================================================

    private String maskRrn(String rrn) {

        if (rrn == null || rrn.isBlank()) {
            return null;
        }

        String numbers =
                rrn.replaceAll("[^0-9]", "");

        if (numbers.length() == 13) {
            return numbers.substring(0, 6)
                    + "-******";
        }

        return "*******";
    }

    // =========================================================
    // 전화번호 마스킹
    // =========================================================

    private String maskPhone(String phone) {

        if (phone == null || phone.isBlank()) {
            return null;
        }

        String numbers =
                phone.replaceAll("[^0-9]", "");

        if (numbers.length() == 11) {

            return numbers.substring(0, 3)
                    + "-****-"
                    + numbers.substring(7);
        }

        return "****";
    }

    // =========================================================
    // 이메일 마스킹
    // =========================================================

    private String maskEmail(String email) {

        if (email == null || email.isBlank()) {
            return null;
        }

        int at =
                email.indexOf("@");

        if (at <= 0) {
            return "****";
        }

        return email.substring(0, 1)
                + "****"
                + email.substring(at);
    }

    private ForbiddenWord findMatchingForbiddenWord(String question) {
        if (question == null || question.isBlank()) {
            return null;
        }

        String normalizedQuestion = question
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("\\s+", "");

        // 1. DB에 등록된 활성 유사 표현을 먼저 검색
        ForbiddenWord mappedWord = forbiddenWordMappingRepository
                .findByActiveTrue()
                .stream()
                .filter(mapping -> mapping.getMappedWord() != null
                        && !mapping.getMappedWord().isBlank())
                .filter(mapping -> mapping.getForbiddenWord() != null
                        && mapping.getForbiddenWord().isActive())
                .filter(mapping -> normalizedQuestion.contains(
                        mapping.getMappedWord()
                                .toLowerCase(java.util.Locale.ROOT)
                                .replaceAll("\\s+", "")
                ))
                .max(java.util.Comparator.comparingInt(
                        mapping -> mapping.getMappedWord().length()
                ))
                .map(ForbiddenWordMapping::getForbiddenWord)
                .orElse(null);

        if (mappedWord != null) {
            return mappedWord;
        }

        // 2. 기존 DB 금지어 검색은 그대로 유지
        return forbiddenWordRepository.findByActiveTrueOrderByIdAsc()
                .stream()
                .filter(w -> w.getWord() != null && !w.getWord().isBlank())
                .filter(w -> normalizedQuestion.contains(
                        w.getWord()
                                .toLowerCase(java.util.Locale.ROOT)
                                .replaceAll("\\s+", "")
                ))
                .max(java.util.Comparator.comparingInt(
                        w -> w.getWord().length()
                ))
                .orElse(null);
    }


    private boolean isEducationalQuestion(String question) {
        String q = question.toLowerCase();
        return q.matches("(?s).*(설명|원리|개념|역사|이론|분석|비교|차이|뜻|정의|공부|학습|알고 싶|궁금|왜 그런지|어떤 의미|무엇인지).*");
    }

    private boolean isActionSeekingQuestion(String question) {
        String q = question.toLowerCase();
        return q.matches("(?s).*(방법을 알려|하는 법|만드는 법|제작 방법|실행 방법|사용 방법|어떻게\\s*(만들|사용|실행|해치|공격|죽이)|따라 하는 방법).*");
    }

    private boolean isContextSensitiveCategory(String category) {
        String c = category == null ? "" : category.toLowerCase();
        return c.contains("학습 방해") || c.contains("폭력") || c.contains("위험");
    }

    private String detectPolicyCategory(String question) {

        if (question.matches("(?s).*(\\d{6}-?[1-4]\\d{6}|01[0-9]-?\\d{3,4}-?\\d{4}).*")) {
            return "개인정보";
        }

        if (question.matches("(?s).*(죽이는 방법|폭력 관련 요청|무기 제작|위험 행동 방법).*")) {
            return "폭력·위험 요청";
        }

        if (question.matches("(?s).*(유튜브|게임|연예인|유명인).*")) {
            return "학습 방해 요소";
        }

        if (question.matches("(?s).*(숙제 대신|과제 대신|시험 답만).*")) {
            return "학습 부정행위";
        }

        return "일반 학습 질문";
    }

    private String getPolicyFromDatabase(String category) {
        return policyRepository.findByCategory(category)
                .filter(AiPolicySetting::isActive)
                .map(AiPolicySetting::getPolicy)
                .orElse(null);
    }

    private void saveGuardrailLog(
            String question,
            String requestedType,
            String risk,
            String action,
            boolean blocked) {

        GuardrailLog log = new GuardrailLog();

        // 개인정보가 포함된 질문은 기존 마스킹 기능을 활용
        log.setQuestion(protectPersonalInfo(question));
        log.setRequestedType(requestedType);
        log.setRisk(risk);
        log.setSecurityAction(action);
        log.setBlocked(blocked);

        logRepository.save(log);
    }
}
