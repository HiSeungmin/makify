package com.xladmt.makify.verification.service;

import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.common.validator.VerifyValidator;
import com.xladmt.makify.verification.dto.VerifyResponse;
import com.xladmt.makify.verification.repository.ChallengeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRecordRepository challengeRecordRepository;
    private final VerifyValidator verifyValidator;


    public VerifyResponse getVerifyPage(Long challengeId, Long memberId) {

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        UserChallenge userChallenge = userChallengeRepository.findByMemberIdAndChallengeId(memberId, challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CHALLENGE_NOT_FOUND));

        int targetFrequency = userChallenge.getTargetFrequency() == null ? 1 : userChallenge.getTargetFrequency();
        int todayVerifiedCount = challengeRecordRepository.countTodayVerifications(
                memberId, challengeId, LocalDate.now());

        return new VerifyResponse(challenge, challenge.getVerificationMethod(), targetFrequency, todayVerifiedCount);
    }

    public void validateVerifyTime(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        verifyValidator.validate(challenge.getVerificationMethod());
    }
}
