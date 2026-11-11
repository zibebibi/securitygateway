package com.club.securitygateway.controller;

import com.club.securitygateway.entity.Account;
import com.club.securitygateway.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    public record AuthRequest(
            String loginId,
            String password,
            String name
    ) {}

    public record AuthResponse(
            Long id,
            String loginId,
            String name,
            String role
    ) {
        public static AuthResponse from(Account account) {
            return new AuthResponse(
                    account.getId(),
                    account.getLoginId(),
                    account.getName(),
                    account.getRole()
            );
        }
    }

    public record ErrorResponse(String message) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody AuthRequest request
    ) {
        try {
            if (request == null) {
                throw new IllegalArgumentException(
                        "입력값을 확인해 주세요."
                );
            }

            Account account = accountService.register(
                    request.loginId(),
                    request.password(),
                    request.name()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.from(account));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register-child")
    public ResponseEntity<?> registerChild(
            @RequestBody AuthRequest request
    ) {
        try {
            if (request == null) {
                throw new IllegalArgumentException(
                        "입력값을 확인해 주세요."
                );
            }

            Account account = accountService.registerChild(
                    request.loginId(),
                    request.password(),
                    request.name()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.from(account));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody AuthRequest request
    ) {
        try {
            if (request == null) {
                throw new IllegalArgumentException(
                        "입력값을 확인해 주세요."
                );
            }

            Account account = accountService.login(
                    request.loginId(),
                    request.password()
            );

            return ResponseEntity.ok(
                    AuthResponse.from(account)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}