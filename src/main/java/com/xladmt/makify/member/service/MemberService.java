package com.xladmt.makify.member.service;

import com.xladmt.makify.common.constant.Role;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.member.dto.SignupRequest;
import com.xladmt.makify.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    public Member signup(SignupRequest request) {

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Member 엔티티 생성
        Member member = Member.create(
                request.getLoginId(),
                encodedPassword,
                Role.USER,
                request.getName(),
                request.getNickname(),
                request.getEmail(),
                null,
                request.getPhone()
        );

        // 저장
        return memberRepository.save(member);
    }
}
