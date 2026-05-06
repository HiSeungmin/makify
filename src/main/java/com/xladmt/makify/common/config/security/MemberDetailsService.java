package com.xladmt.makify.common.config.security;

import com.xladmt.makify.member.domain.Member;
import com.xladmt.makify.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        log.info("🔍 로그인 시도 ID: " + loginId);
        Member member = memberRepository.findByLoginId(loginId)
                        .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));
        return new MemberDetails(member);
    }
}