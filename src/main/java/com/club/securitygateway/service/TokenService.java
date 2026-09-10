package com.club.securitygateway.service;

import com.club.securitygateway.entity.Token;
import com.club.securitygateway.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    // 토큰 저장
    public Token save(Token token) {
        return tokenRepository.save(token);
    }

    // 토큰 전체 조회
    public List<Token> findAll() {
        return tokenRepository.findAll();
    }

    // 토큰 1개 조회
    public Token findById(Long id) {
        return tokenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("토큰을 찾을 수 없습니다."));
    }

    // 주민등록번호 토큰화
    public String tokenizeRRN(String rrn) {

        if (rrn == null || rrn.isBlank()) {
            return null;
        }

        // 기존 토큰이 있는지 확인
        for (Token token : tokenRepository.findAll()) {

            if (rrn.equals(token.getOriginalValue())
                    && token.getTokenValue().startsWith("RRN_")) {

                return token.getTokenValue();
            }
        }

        // 새로운 토큰 생성
        String tokenValue =
                String.format("RRN_%03d", tokenRepository.count() + 1);

        Token token = new Token();
        token.setOriginalValue(rrn);
        token.setTokenValue(tokenValue);

        tokenRepository.save(token);

        return tokenValue;
    }

    // 전화번호 토큰화
    public String tokenizePhone(String phone) {

        if (phone == null || phone.isBlank()) {
            return null;
        }

        // 기존 토큰이 있는지 확인
        for (Token token : tokenRepository.findAll()) {

            if (phone.equals(token.getOriginalValue())
                    && token.getTokenValue().startsWith("TEL_")) {

                return token.getTokenValue();
            }
        }

        // 새로운 토큰 생성
        String tokenValue =
                String.format("TEL_%03d", tokenRepository.count() + 1);

        Token token = new Token();
        token.setOriginalValue(phone);
        token.setTokenValue(tokenValue);

        tokenRepository.save(token);

        return tokenValue;
    }
}