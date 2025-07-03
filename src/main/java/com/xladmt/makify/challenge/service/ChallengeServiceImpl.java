package com.xladmt.makify.challenge.service;


import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.dto.ChallengeDetailResponse;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.VerificationMethodRepository;
import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.Frequency;
import com.xladmt.makify.common.constant.VerificationType;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.entity.VerificationMethod;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;
    private final VerificationMethodRepository verificationMethodRepository;


    public List<Challenge> getAllVisibleChallenges() {
        return challengeRepository.findAll()
                .stream()
                .filter(challenge -> challenge.getIsVisible().name().equals("Y"))
                .toList();
    }


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
        // 누락된 저장 로직
        verificationMethodRepository.save(verificationMethod);

        // 이미지 파일 처리 필요

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

    public ChallengeDetailResponse getChallenge(Long id) {
        Challenge challenge = challengeRepository.findByIdWithMember(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        VerificationMethod verificationMethod = verificationMethodRepository.findById(challenge.getVerificationMethod().getId())
                .orElseThrow(()-> new BusinessException(ErrorCode.VERIFICATION_METHOD_NOT_FOUND));

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
                .build();
    }

    public Challenge join (Long id){
        return challengeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

    }
}
