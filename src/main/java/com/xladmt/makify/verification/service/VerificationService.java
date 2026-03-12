package com.xladmt.makify.verification.service;

import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.config.fileUpload.S3Uploader;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.ChallengeRecord;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.common.validator.VerifyValidator;
import com.xladmt.makify.member.repository.MemberRepository;
import com.xladmt.makify.verification.dto.VerifyResponse;
import com.xladmt.makify.verification.repository.ChallengeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRecordRepository challengeRecordRepository;
    private final MemberRepository memberRepository;
    private final VerifyValidator verifyValidator;
    private final S3Uploader s3Uploader;

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

    @Transactional
    public void verify(Long challengeId, Long memberId, MultipartFile image, String memo) throws IOException {

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // S3 업로드 - verification/{challengeId}/{memberId} 경로에 저장
        String folder = "verification/" + challengeId + "/" + memberId;
        String key = s3Uploader.uploadAndGetKey(image, folder);

        try {
            // 인증 내역 저장
            String imageUrl = s3Uploader.generatePresignedUrl(key);
            ChallengeRecord record = ChallengeRecord.create(member, challenge, imageUrl, memo);
            challengeRecordRepository.save(record);
        } catch (Exception e) {
            // DB 저장 실패 시 S3 파일 삭제 (고아 파일 방지)
            log.error("[VerificationService] DB 저장 실패로 S3 파일 삭제 - key: {}", key);
            s3Uploader.delete(key);
            throw e;
        }
    }
}
