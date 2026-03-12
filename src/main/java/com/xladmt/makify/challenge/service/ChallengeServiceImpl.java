package com.xladmt.makify.challenge.service;

import com.xladmt.makify.challenge.dto.*;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.challenge.repository.VerificationMethodRepository;
import com.xladmt.makify.common.config.fileUpload.S3Uploader;
import com.xladmt.makify.common.constant.Frequency;
import com.xladmt.makify.common.constant.ImageType;
import com.xladmt.makify.common.constant.PaidStatus;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.*;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.common.repository.FileMetaRepository;
import com.xladmt.makify.member.repository.MemberRepository;
import com.xladmt.makify.payment.dto.RequestPayDto;
import com.xladmt.makify.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final VerificationMethodRepository verificationMethodRepository;
    private final PaymentRepository paymentRepository;
    private final FileMetaRepository fileMetaRepository;
    private final ChallengeMapper challengeMapper;
    private final S3Uploader s3Uploader;

    private static final String DEFAULT_THUMBNAIL = "/images/default-challenge.png";

    @Override
    @Transactional(readOnly = true)
    public List<Challenge> getAllVisibleChallenges() {
        return challengeRepository.findAllByIsVisibleOrderByCreatedAtDesc(YN.Y);
    }

    @Override
    public ChallengePageDto getChallengesPage(List<Challenge> allChallenges, int page, int size) {
        int totalElements = allChallenges.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<Challenge> pagedChallenges = start < totalElements ?
                allChallenges.subList(start, end) : new ArrayList<>();

        ChallengePageDto pageDto = new ChallengePageDto();
        pageDto.setChallenges(pagedChallenges);
        pageDto.setTotalPages(totalPages);
        pageDto.setTotalElements(totalElements);
        pageDto.setCurrentPage(page);
        pageDto.setHasNext(page < totalPages - 1);
        pageDto.setHasPrevious(page > 0);
        return pageDto;
    }

    @Override
    @Transactional
    public void create(ChallengeCreateRequest request, Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 인증 방법 생성
        VerificationMethod verificationMethod = VerificationMethod.create(
                Frequency.valueOf(request.getFrequency()),
                request.getStartTime(),
                request.getEndTime(),
                request.getMinDailyCount(),
                request.getVerificationType(),
                request.getExampleImages()==null?YN.N:YN.Y
        );
        verificationMethodRepository.save(verificationMethod);

        // 대표 이미지 업로드 → URL 직접 저장 (1장)
        String thumbnailUrl = DEFAULT_THUMBNAIL;
        if (request.getThumbnailImage() != null && !request.getThumbnailImage().isEmpty()) {
            try {
                thumbnailUrl = s3Uploader.upload(request.getThumbnailImage(), "challenge/thumbnail");
            } catch (Exception e) {
                new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
            }
        }

        // 챌린지 생성
        Challenge challenge = Challenge.create(
                member,
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getIsPublic(),
                request.getIsFixedDeposit(),
                request.getMaxDeposit() == null ? request.getFixedDeposit() : request.getMaxDeposit(),
                verificationMethod,
                request.getPrivateCode(),
                request.getMaxParticipants(),
                request.getCategory(),
                thumbnailUrl
        );
        challengeRepository.save(challenge);

        // 인증 예시 이미지 업로드 → FileMeta로 저장 (최대 3장)
        if (request.getExampleImages() != null && !request.getExampleImages().isEmpty()) {
            String folder = "challenge/example/" + verificationMethod.getId();
            List<FileMeta> fileMetas = new ArrayList<>();

            for (MultipartFile img : request.getExampleImages()) {
                if (img == null || img.isEmpty() || fileMetas.size() >= 3) continue;
                try {
                    String key = s3Uploader.uploadAndGetKey(img, folder);
                    String ext = extractExtension(img.getOriginalFilename());
                    fileMetas.add(FileMeta.create(
                            verificationMethod.getId(),
                            ImageType.VERIFICATION_EXAMPLE,
                            img.getOriginalFilename(),
                            key,
                            String.valueOf(img.getSize()),
                            ext
                    ));
                } catch (Exception e) {
                    new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
                }
            }

            if (!fileMetas.isEmpty()) {
                fileMetaRepository.saveAll(fileMetas);
                verificationMethod.markHasExampleImage(); // YN.Y로 변경
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChallengeDetailResponse getChallenge(String loginId, Long challengeId) {
        Challenge challenge = challengeRepository.findByIdWithMember(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        boolean alreadyJoined = false;
        if (loginId != null) {
            Long memberId = memberRepository.findByLoginId(loginId).get().getId();
            alreadyJoined = hasJoinedChallenge(challenge.getId(), memberId);
        }

        VerificationMethod verificationMethod = verificationMethodRepository.findById(challenge.getVerificationMethod().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_METHOD_NOT_FOUND));

        Long currentParticipants = getParticipantCount(challenge.getId());

        // 대표 이미지
        String thumbnailUrl = challenge.getThumbnailUrl();

        // 인증 예시 이미지 (hasExampleImage == Y 일 때만 조회)
        List<String> exampleImageUrls = List.of();
        if (verificationMethod.getHasExampleImage() == YN.Y) {
            exampleImageUrls = fileMetaRepository
                    .findByReferenceIdAndTypeAndIsDeleted(verificationMethod.getId(), ImageType.VERIFICATION_EXAMPLE, YN.N)
                    .stream()
                    .map(f -> s3Uploader.getPublicUrl(f.getPath()))
                    .toList();
        }

        return ChallengeDetailResponse.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .maxParticipants(challenge.getMaxParticipants())
                .isFixedDeposit(challenge.getIsFixedDeposit())
                .maxDeposit(challenge.getMaxDeposit())
                .status(challenge.getStatus())
                .creatorLoginId(challenge.getMember().getLoginId())
                .creatorNickName(challenge.getMember().getNickname())
                .frequencyLabel(verificationMethod.getFrequency().getLabel())
                .startTime(verificationMethod.getStartTime())
                .endTime(verificationMethod.getEndTime())
                .minDailyCount(verificationMethod.getMinDailyCount())
                .verificationType(verificationMethod.getMethod().getDescription())
                .category(challenge.getCategory().getDescription())
                .alreadyJoined(alreadyJoined)
                .currentParticipants(currentParticipants)
                .progressPercentage(challenge.getProgressPercentage())
                .thumbnailUrl(thumbnailUrl)
                .exampleImageUrls(exampleImageUrls)
                .build();
    }

    @Transactional(readOnly = true)
    public long getParticipantCount(Long challengeId) {
        return userChallengeRepository.countByChallengeId(challengeId);
    }

    public boolean hasJoinedChallenge(Long challengeId, Long memberId) {
        return userChallengeRepository.existsJoinedByChallengeIdAndMemberId(challengeId, memberId);
    }

    @Override
    @Transactional
    public String createPendingUserChallenge(Long challengeId, Long userId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        BigDecimal paymentAmount = BigDecimal.valueOf(100);
        Payment payment = Payment.create(paymentAmount, PaidStatus.PENDING);
        paymentRepository.save(payment);

        String uuid = "imp_" + UUID.randomUUID();
        UserChallenge userChallenge = UserChallenge.createUserChallenge(challenge, member, payment, uuid);
        userChallengeRepository.save(userChallenge);
        return uuid;
    }

    @Override
    @Transactional
    public void completeUserChallenge(String uuid) {
        UserChallenge userChallenge = userChallengeRepository.findByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CHALLENGE_NOT_FOUND));
        userChallenge.markAsJoined();

        Challenge challenge = challengeRepository.findById(userChallenge.getChallenge().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
        challenge.incrementParticipantCount();
    }

    @Override
    @Transactional
    public void failUserChallenge(String uuid) {
        UserChallenge userChallenge = userChallengeRepository.findByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CHALLENGE_NOT_FOUND));
        userChallenge.markAsFailed();
    }

    @Override
    @Transactional(readOnly = true)
    public RequestPayDto getPaymentInfo(Long challengeId, Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        return RequestPayDto.builder()
                .uuid(null)
                .buyerName(member.getName())
                .buyerEmail(member.getEmail())
                .build();
    }

    @Override
    public ChallengeSearchResponse searchChallenges(ChallengeSearchRequest request) {
        request.setDefaultsIfNull();
        Sort sort = createSort(request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<ChallengeSearchDto> resultPage = challengeRepository.searchChallenges(
                request.getKeyword(),
                request.getCategory(),
                pageable
        ).map(challengeMapper::toSearchDto);

        return ChallengeSearchResponse.from(resultPage, request);
    }

    private Sort createSort(String sortBy) {
        return switch (sortBy) {
            case "popularity" -> Sort.by("participantCount").descending();
            case "latest" -> Sort.by("createdAt").descending();
            case "startDate" -> Sort.by("startDate").ascending();
            default -> Sort.by("createdAt").descending();
        };
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
