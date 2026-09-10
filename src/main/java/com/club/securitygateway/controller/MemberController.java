package com.club.securitygateway.controller;

import com.club.securitygateway.entity.Member;
import com.club.securitygateway.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 회원 등록
    @PostMapping
    public Member save(@RequestBody Member member) {
        return memberService.save(member);
    }

    // 회원 전체 조회
    @GetMapping
    public List<Member> findAll() {
        return memberService.findAll();
    }

    // 회원 이름 검색
    @GetMapping("/search")
    public List<Member> searchByName(@RequestParam String name) {
        return memberService.searchByName(name);
    }

    // 회원 1명 조회
    @GetMapping("/{id}")
    public Member findById(@PathVariable Long id) {
        return memberService.findById(id);
    }

    // 회원 수정
    @PutMapping("/{id}")
    public Member update(@PathVariable Long id, @RequestBody Member member) {
        return memberService.update(id, member);
    }

    // 회원 삭제
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        memberService.delete(id);
        return "회원이 삭제되었습니다.";
    }
}