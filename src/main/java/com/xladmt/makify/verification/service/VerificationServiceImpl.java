package com.xladmt.makify.verification.service;

import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.config.fileUpload.S3Uploader;
import com.xladmt.makify.common.constant.ImageType;
import com.xladmt.makify.common.constant.NotificationType;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.challenge.domain.Challenge;
import com.xladmt.makify.challenge.domain.ChallengeRecord;
import com.xladmt.makify.common.entity.FileMeta;
import com.xladmt.makify.member.domain.Member;
import com.xladmt.makify.challenge.domain.UserChallenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.common.repository.FileMetaRepository;
import com.xladmt.makify.common.validator.VerifyValidator;
import com.xladmt.makify.member.repository.MemberRepository;
import com.xladmt.makify.notification.service.NotificationService;
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
    private final NotificationService notificationService;

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

    @Override
    public void validateVerifyTime(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        verifyValidator.validate(challenge.getVerificationMethod());
    }

    @Override
    public VerifyResponse getHistoryPage(Long challengeId, Long memberId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        UserChallenge userChallenge = userChallengeRepository.findByMemberIdAndChallengeId(memberId, challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CHALLENGE_NOT_FOUND));

        int targetFrequency = userChallenge.getTargetFrequency() == null ? 1 : userChallenge.getTargetFrequency();
        int todayVerifiedCount = challengeRecordRepository.countTodayVerifications(memberId, challengeId, LocalDate.now());

        return new VerifyResponse(challenge, challenge.getVerificationMethod(), targetFrequency, todayVerifiedCount);
    }

    @Override
    public List<HistoryResponse> getRecords(Long challengeId, Long memberId) {
        return challengeRecordRepository.findAllByMemberAndChallenge(memberId, challengeId)
                .stream()
                .map(HistoryResponse::from)
                .toList();
    }

    @Override
    public List<HistoryResponse> getOtherRecords(Long challengeId, Long memberId) {
        return challengeRecordRepository.findAllByOtherMembers(memberId, challengeId)
                .stream()
                .map(HistoryResponse::from)
                .toList();
    }

    @Override
    public int getTotalCount(Long challengeId, Long memberId) {
        return challengeRecordRepository.countAllVerifications(memberId, challengeId);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    @Override
    @Transactional
    public void verify(Long challengeId, Long memberId, MultipartFile image, boolean isPublic, String memo) throws IOException {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String folder = "verification/" + challengeId + "/" + memberId;
        String key = s3Uploader.uploadAndGetKey(image, folder);
        YN isPublicYn = isPublic ? YN.Y : YN.N;

        try {
            String imageUrl = s3Uploader.getPublicUrl(key);
            ChallengeRecord record = ChallengeRecord.create(member, challenge, imageUrl, memo, isPublicYn);
            challengeRecordRepository.save(record);

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

            // 인증 완료 알림 발행
            String redirectUrl = "/challenges/" + challengeId + "/history";
            notificationService.send(
                    memberId,
                    NotificationType.VERIFICATION_APPROVED,
                    "'" + challenge.getTitle() + "' " + NotificationType.VERIFICATION_APPROVED.getDefaultMessage(),
                    redirectUrl
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            s3Uploader.delete(key);
            throw new BusinessException(ErrorCode.FILE_META_DB_UPLOAD_FAIL);
        }
    }

    @Override
    @Transactional
    public void deleteVerify(long recordId, long memberId) {
        ChallengeRecord challengeRecord = challengeRecordRepository.findByIdAndMemberId(recordId, memberId);
        challengeRecord.delete();
    }

    @Override
    @Transactional
    public boolean togglePublic(long recordId, long memberId) {
        ChallengeRecord record = challengeRecordRepository.findByIdAndMemberId(recordId, memberId);
        record.togglePublic();
        return YN.Y.equals(record.getIsPublic());
    }
}
