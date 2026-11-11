package com.club.securitygateway.service;

import com.club.securitygateway.entity.Account;
import com.club.securitygateway.repository.AccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account register(String loginId, String password, String name) {
        validateInput(loginId, password, name);

        loginId = loginId.trim();
        name = name.trim();

        if (accountRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        Account account = new Account();
        account.setLoginId(loginId);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setName(name);
        account.setRole("parent");

        return accountRepository.save(account);
    }

    @Transactional
    public Account registerChild(
            String loginId,
            String password,
            String name
    ) {
        validateInput(loginId, password, name);

        loginId = loginId.trim();
        name = name.trim();

        if (accountRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        Account account = new Account();
        account.setLoginId(loginId);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setName(name);
        account.setRole("child");

        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account login(String loginId, String password) {
        if (loginId == null || password == null
                || loginId.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException(
                    "아이디와 비밀번호를 확인해 주세요."
            );
        }

        Account account = accountRepository.findByLoginId(loginId.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "아이디 또는 비밀번호가 올바르지 않습니다."
                ));

        if (!passwordEncoder.matches(
                password,
                account.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        return account;
    }

    private void validateInput(
            String loginId,
            String password,
            String name
    ) {
        if (loginId == null || loginId.isBlank()
                || password == null || password.isBlank()
                || name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "모든 항목을 입력해 주세요."
            );
        }

        if (password.length() < 4) {
            throw new IllegalArgumentException(
                    "비밀번호를 4자 이상 입력해 주세요."
            );
        }
    }
}