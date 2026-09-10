package com.club.securitygateway.service;

import com.club.securitygateway.entity.Member;
import com.club.securitygateway.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원 등록
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    // 회원 전체 조회
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // 회원 1명 조회
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    }

    // 이름으로 회원 검색
    public List<Member> searchByName(String name) {
        return memberRepository.findByNameContaining(name);
    }

    // 회원 수정
    public Member update(Long id, Member member) {
        Member findMember = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        findMember.setName(member.getName());
        findMember.setRrn(member.getRrn());
        findMember.setPhone(member.getPhone());
        findMember.setEmail(member.getEmail());
        findMember.setClub(member.getClub());
        findMember.setJoinDate(member.getJoinDate());

        return memberRepository.save(findMember);
    }

    // 회원 삭제
    public void delete(Long id) {
        memberRepository.deleteById(id);
    }
}