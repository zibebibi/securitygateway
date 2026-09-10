package com.club.securitygateway.controller;

import com.club.securitygateway.entity.Token;
import com.club.securitygateway.service.TokenService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tokens")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    // 토큰 저장
    @PostMapping
    public Token save(@RequestBody Token token) {
        return tokenService.save(token);
    }

    // 토큰 전체 조회
    @GetMapping
    public List<Token> findAll() {
        return tokenService.findAll();
    }

    // 토큰 1개 조회
    @GetMapping("/{id}")
    public Token findById(@PathVariable Long id) {
        return tokenService.findById(id);
    }
}