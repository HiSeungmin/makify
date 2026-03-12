package com.xladmt.makify.verification.service;

import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.config.fileUpload.S3Uploader;
import com.xladmt.makify.common.constant.ImageType;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.ChallengeRecord;
import com.xladmt.makify.common.entity.FileMeta;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.common.repository.FileMetaRepository;
import com.xladmt.makify.common.validator.VerifyValidator;
import com.xladmt.makify.member.repository.MemberRepository;
import com.xladmt.makify.verification.dto.HistoryResponse;
import com.xladmt.makify.verification.dto.VerifyResponse;
import com.xladmt.makify.verification.repository.ChallengeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRecordRepository challengeRecordRepository;
    private final FileMetaRepository fileMetaRepository;
    private final MemberRepository memberRepository;
    private final VerifyValidator verifyValidator;
    private final S3Uploader s3Uploader;

    @Override
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

    public VerifyResponse getHistoryPage(Long challengeId, Long memberId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        UserChallenge userChallenge = userChallengeRepository.findByMemberIdAndChallengeId(memberId, challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CHALLENGE_NOT_FOUND));

        int targetFrequency = userChallenge.getTargetFrequency() == null ? 1 : userChallenge.getTargetFrequency();
        int todayVerifiedCount = challengeRecordRepository.countTodayVerifications(memberId, challengeId, LocalDate.now());

        return new VerifyResponse(challenge, challenge.getVerificationMethod(), targetFrequency, todayVerifiedCount);
    }

    public List<HistoryResponse> getRecords(Long challengeId, Long memberId) {
        return challengeRecordRepository.findAllByMemberAndChallenge(memberId, challengeId)
                .stream()
                .map(HistoryResponse::from)
                .toList();
    }

    public int getTotalCount(Long challengeId, Long memberId) {
        return challengeRecordRepository.countAllVerifications(memberId, challengeId);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
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
            String imageUrl = s3Uploader.getPublicUrl(key);
            ChallengeRecord record = ChallengeRecord.create(member, challenge, imageUrl, memo);
            challengeRecordRepository.save(record);

            // FileMeta 저장
            String ext = extractExtension(image.getOriginalFilename());
            FileMeta fileMeta = FileMeta.create(
                    record.getId(),
                    ImageType.VERIFICATION,
                    image.getOriginalFilename(),
                    key,
                    String.valueOf(image.getSize()),
                    ext
            );
            fileMetaRepository.save(fileMeta);

        } catch (Exception e) {
            // DB 저장 실패 시 S3 파일 삭제 (고아 파일 방지)
            new BusinessException(ErrorCode.FILE_META_DB_UPLOAD_FAIL);
            s3Uploader.delete(key);
            throw e;
        }
    }
}
