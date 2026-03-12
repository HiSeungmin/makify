package com.xladmt.makify.member.service;


import com.xladmt.makify.verification.repository.ChallengeRecordRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.constant.ChallengeStatus;
import com.xladmt.makify.common.constant.Role;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.dto.*;
import com.xladmt.makify.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRecordRepository challengeRecordRepository;


    @Override
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


    @Override
    public MypageResponse mypage(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));


        return new MypageResponse(member.getNickname(), "");
    }

    @Override
    public MyChallengeResponse myChallenge(Long memberId) {
        List<UserChallenge> userChallenges = userChallengeRepository.findByMemberId(memberId);

        Map<ChallengeStatus, List<UserChallenge>> grouped = userChallenges.stream()
                .collect(Collectors.groupingBy(uc -> uc.getChallenge().getStatus()));

        return new MyChallengeResponse(
                grouped.getOrDefault(ChallengeStatus.IN_PROGRESS, List.of()),
                grouped.getOrDefault(ChallengeStatus.NOT_STARTED, List.of()),
                grouped.getOrDefault(ChallengeStatus.COMPLETED, List.of()),
                uc -> challengeRecordRepository.countTodayVerifications(
                        memberId, uc.getChallenge().getId(), LocalDate.now())
        );
    }

    public MyReviewResponse myReview(){return null;}
    public MyInquiryResponse myInquiry(){return null;}
}
