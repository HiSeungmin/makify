package com.xladmt.makify.challenge.service;


import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.dto.ChallengeDetailResponse;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.challenge.repository.VerificationMethodRepository;
import com.xladmt.makify.common.constant.Frequency;
import com.xladmt.makify.common.constant.PaidStatus;
import com.xladmt.makify.common.constant.UserChallengeStatus;
import com.xladmt.makify.common.entity.*;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.repository.MemberRepository;
import com.xladmt.makify.payment.dto.RequestPayDto;
import com.xladmt.makify.payment.repository.PaymentRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    @Override
    @Transactional(readOnly = true)
    public List<Challenge> getAllVisibleChallenges() {
        return challengeRepository.findAll()
                .stream()
                .filter(challenge -> challenge.getIsVisible().name().equals("Y"))
                .toList();
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

        Long memberId = memberRepository.findByLoginId(loginId).get().getId();

        VerificationMethod verificationMethod = verificationMethodRepository.findById(challenge.getVerificationMethod().getId())
                .orElseThrow(()-> new BusinessException(ErrorCode.VERIFICATION_METHOD_NOT_FOUND));

        boolean alreadyJoined = hasJoinedChallenge(challenge.getId(), memberId);
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
    public Challenge join (Long memberId, Long id){

//        Challenge challenge = challengeRepository.findById(id)
//                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
//
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
//
//        // TODO 가격 임시로 설정 **
//        Payment payment = Payment.create(100L, PaidStatus.PENDING);
//
//        paymentRepository.save(payment);
//
//        UserChallenge userChallenge = UserChallenge.createUserChallenge(challenge, member, payment, UUID.randomUUID().toString());
//
//        userChallengeRepository.save(userChallenge);
//
//        return challenge;
        return null;
    }


//    @Deprecated
//    public RequestPayDto getRequestPayDto(Long challengeId, Long memberId) {
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
//
//        UserChallenge userChallenge = userChallengeRepository
//                .findByChallengeIdAndMemberId(challengeId, member.getId())
//                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CHALLENGE_NOT_FOUND));
//
//        return RequestPayDto.builder()
//                .uuid(userChallenge.getUuid())
//                .buyerName(member.getName())
//                .buyerEmail(member.getEmail())
//                .build();
//    }

    @Override
    @Transactional(readOnly = true)
    public void validateJoinable(Long challengeId, Long userId) {
        // 1. 챌린지 존재 확인
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
        
        // 2. 사용자 존재 확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        // 3. 이미 참여했는지 확인
        if (hasJoinedChallenge(challengeId, member.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED_CHALLENGE);
        }
        
        // 4. 참여 가능 인원 확인
        long currentParticipants = getParticipantCount(challengeId);
        if (currentParticipants >= challenge.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.CHALLENGE_FULL);
        }
        
        // 5. 챌린지 시작 전인지 확인 등 추가 검증...
    }

    @Override
    @Transactional
    public String createPendingUserChallenge(Long challengeId, Long userId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
        
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        // 결제 금액 계산 (임시로 100L 설정, 나중에 로직 개선)
        Long paymentAmount = 100L; // TODO: 실제 로직으로 변경
        
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
        
        // 상태를 JOINED로 변경 (UserChallenge 엔티티에 메서드 필요)
        userChallenge.markAsJoined();
    }

    @Override
    @Transactional
    public void updateFailUserChallenge(String uuid) {
        UserChallenge userChallenge = userChallengeRepository.findByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CHALLENGE_NOT_FOUND));
        
        // Payment도 함께 삭제
        Payment payment = userChallenge.getPayment();
        
        userChallengeRepository.delete(userChallenge);
        paymentRepository.delete(payment);
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



}
