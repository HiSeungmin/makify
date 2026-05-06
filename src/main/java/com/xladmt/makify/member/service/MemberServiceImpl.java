package com.xladmt.makify.member.service;

import com.xladmt.makify.challenge.repository.ReviewRepository;
import com.xladmt.makify.verification.repository.ChallengeRecordRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.constant.ChallengeStatus;
import com.xladmt.makify.common.constant.Role;
import com.xladmt.makify.member.domain.Member;
import com.xladmt.makify.challenge.domain.UserChallenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.dto.*;
import com.xladmt.makify.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRecordRepository challengeRecordRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public Member signup(SignupRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
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

        log.debug("[myChallenge] 전체 UserChallenge 수: {}", userChallenges.size());
        userChallenges.forEach(uc -> log.debug(
                "[myChallenge] challengeId={}, title={}, ucStatus={}, challengeStatus={}",
                uc.getChallenge().getId(),
                uc.getChallenge().getTitle(),
                uc.getStatus(),
                uc.getChallenge().getStatus()
        ));

        Map<ChallengeStatus, List<UserChallenge>> grouped = userChallenges.stream()
                .collect(Collectors.groupingBy(uc -> uc.getChallenge().getStatus()));

        log.debug("[myChallenge] IN_PROGRESS 수: {}", grouped.getOrDefault(ChallengeStatus.IN_PROGRESS, List.of()).size());
        log.debug("[myChallenge] NOT_STARTED 수: {}", grouped.getOrDefault(ChallengeStatus.NOT_STARTED, List.of()).size());
        log.debug("[myChallenge] COMPLETED 수: {}", grouped.getOrDefault(ChallengeStatus.COMPLETED, List.of()).size());

        List<UserChallenge> completedList = grouped.getOrDefault(ChallengeStatus.COMPLETED, List.of());

        Set<Long> reviewedChallengeIds = completedList.stream()
                .map(uc -> uc.getChallenge().getId())
                .filter(challengeId -> reviewRepository.existsByMemberIdAndChallengeId(memberId, challengeId))
                .collect(Collectors.toSet());

        log.debug("[myChallenge] 리뷰 작성된 challengeId 목록: {}", reviewedChallengeIds);

        return new MyChallengeResponse(
                grouped.getOrDefault(ChallengeStatus.IN_PROGRESS, List.of()),
                grouped.getOrDefault(ChallengeStatus.NOT_STARTED, List.of()),
                completedList,
                uc -> challengeRecordRepository.countTodayVerifications(
                        memberId, uc.getChallenge().getId(), LocalDate.now()),
                reviewedChallengeIds
        );
    }

    @Override
    public MyReviewResponse myReview(Long memberId) {
        return new MyReviewResponse(reviewRepository.findByMemberIdWithChallenge(memberId));
    }

    public MyInquiryResponse myInquiry() { return null; }
}
