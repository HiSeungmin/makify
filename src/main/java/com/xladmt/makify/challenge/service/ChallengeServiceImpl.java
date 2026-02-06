package com.xladmt.makify.challenge.service;


import com.xladmt.makify.challenge.dto.*;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.challenge.repository.VerificationMethodRepository;
import com.xladmt.makify.common.constant.Frequency;
import com.xladmt.makify.common.constant.PaidStatus;
import com.xladmt.makify.common.constant.UserChallengeStatus;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.*;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.repository.MemberRepository;
import com.xladmt.makify.payment.dto.RequestPayDto;
import com.xladmt.makify.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final VerificationMethodRepository verificationMethodRepository;
    private final PaymentRepository paymentRepository;
    private final ChallengeMapper challengeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Challenge> getAllVisibleChallenges() {
        return challengeRepository
                .findAllByIsVisibleOrderByCreatedAtDesc(YN.Y);
    }

    @Override
    public ChallengePageDto getChallengesPage(List<Challenge> allChallenges, int page, int size) {

        int totalElements = allChallenges.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<Challenge> pagedChallenges = start < totalElements ?
                allChallenges.subList(start, end) : new java.util.ArrayList<>();

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
                request.getVerificationType()
        );
        verificationMethodRepository.save(verificationMethod);

        // TODO: 이미지 파일 처리 필요

        // 챌린지 생성
        Challenge challenge = Challenge.create(
                member,
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getIsPublic(),
                request.getIsFixedDeposit(),
                request.getMaxDeposit()==null? request.getFixedDeposit() : request.getMaxDeposit(),
                verificationMethod,
                request.getPrivateCode(),
                request.getMaxParticipants(),
                request.getCategory()
        );

        challengeRepository.save(challenge);
    }

    @Override
    @Transactional(readOnly = true)
    public ChallengeDetailResponse getChallenge(String loginId, Long challengeId) {
        Challenge challenge = challengeRepository.findByIdWithMember(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        Long memberId = null;
        boolean alreadyJoined = false;

        if(loginId!=null){
            memberId = memberRepository.findByLoginId(loginId).get().getId();
            alreadyJoined = hasJoinedChallenge(challenge.getId(), memberId);
        }

        VerificationMethod verificationMethod = verificationMethodRepository.findById(challenge.getVerificationMethod().getId())
                .orElseThrow(()-> new BusinessException(ErrorCode.VERIFICATION_METHOD_NOT_FOUND));

        Long currentParticipants = getParticipantCount(challenge.getId());

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
                .frequencyLabel(verificationMethod.getFrequency().getLabel())
                .startTime(verificationMethod.getStartTime())
                .endTime(verificationMethod.getEndTime())
                .minDailyCount(verificationMethod.getMinDailyCount())
                .verificationType(verificationMethod.getMethod().getDescription())
                .category(challenge.getCategory().getDescription())
                .alreadyJoined(alreadyJoined)
                .currentParticipants(currentParticipants)
                .progressPercentage(challenge.getProgressPercentage())
                .build();
    }

    @Transactional(readOnly = true)
    public long getParticipantCount(Long challengeId) {
        // TODO: 상태 확인 필요
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

        // TODO: 실제 로직으로 변경
        BigDecimal paymentAmount = BigDecimal.valueOf(100);
        
        // PENDING 상태로 Payment 생성
        Payment payment = Payment.create(paymentAmount, PaidStatus.PENDING);
        paymentRepository.save(payment);
        
        // PENDING 상태로 UserChallenge 생성
        String uuid = UUID.randomUUID().toString();
        UserChallenge userChallenge = UserChallenge.createUserChallenge(challenge, member, payment, uuid);
        userChallengeRepository.save(userChallenge);
        
        return uuid;
    }

    @Override
    @Transactional
    public void completeUserChallenge(String uuid) {
        UserChallenge userChallenge = userChallengeRepository.findByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CHALLENGE_NOT_FOUND));
        
        // 상태를 JOINED로 변경
        userChallenge.markAsJoined();

        // 참여자 수 증가
        Challenge challenge = challengeRepository.findById(userChallenge.getChallenge().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
        challenge.incrementParticipantCount();
    }

    @Override
    @Transactional
    public void failUserChallenge(String uuid) {
        UserChallenge userChallenge = userChallengeRepository.findByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CHALLENGE_NOT_FOUND));

        // 상태를 FAIL로 변경
        userChallenge.markAsFailed();
    }

    @Override
    @Transactional(readOnly = true)
    public RequestPayDto getPaymentInfo(Long challengeId, Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
        
        // uuid 없이 결제 정보만 반환
        return RequestPayDto.builder()
                .uuid(null) // 결제 초기화할 때 생성될 예정
                .buyerName(member.getName())
                .buyerEmail(member.getEmail())
                .build();
    }


    @Override
    public ChallengeSearchResponse searchChallenges(ChallengeSearchRequest request) {
        // 기본값 설정
        request.setDefaultsIfNull();

        // 정렬 조건 생성
        Sort sort = createSort(request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // 검색 실행
        Page<ChallengeSearchDto> resultPage = challengeRepository.searchChallenges(
                request.getKeyword(),
                request.getCategory(),
                pageable
        ).map(challengeMapper::toSearchDto);

        // 응답 생성
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

}
