package com.xladmt.makify.common.validator;

import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ChallengeValidator {

    private final ChallengeRepository challengeRepository;
    private final MemberRepository memberRepository;
    private final UserChallengeRepository userChallengeRepository;


    /**
     * 챌린지 참여 가능성 종합 검증
     */
    public void validateJoinable(Long challengeId, Long userId) {
        Challenge challenge = validateChallengeExists(challengeId);
        Member member = validateMemberExists(userId);

        validateNotAlreadyJoined(challengeId, member.getId());
        validateChallengeCapacity(challenge);
        validateChallengeStartDate(challenge);
    }

    /**
     * 챌린지 존재 검증
     */
    public Challenge validateChallengeExists(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        return challenge;
    }

    /**
     * 사용자 존재 검증
     */
    public Member validateMemberExists(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return member;
    }

    /**
     * 중복 참여 검증
     */
    public void validateNotAlreadyJoined(Long challengeId, Long userId) {
        boolean alreadyJoined = userChallengeRepository.existsJoinedByChallengeIdAndMemberId(challengeId, userId);

        if (alreadyJoined) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED_CHALLENGE);
        }
    }

    /**
     * 참여 인원 제한 검증
     */
    public void validateChallengeCapacity(Challenge challenge) {
        long currentParticipants = userChallengeRepository.countByChallengeId(challenge.getId());

        if (currentParticipants >= challenge.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.CHALLENGE_FULL);
        }
    }

    /**
     * 챌린지 시작일 검증
     */
    public void validateChallengeStartDate(Challenge challenge) {
        LocalDate startDate = challenge.getStartDate();
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today)) {
            throw new BusinessException(ErrorCode.CHALLENGE_ALREADY_STARTED);
        }

    }

}
