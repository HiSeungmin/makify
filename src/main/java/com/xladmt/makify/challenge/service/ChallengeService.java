package com.xladmt.makify.challenge.service;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.dto.ChallengeDetailResponse;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.payment.dto.RequestPayDto;

import java.util.List;

public interface ChallengeService {
    List<Challenge> getAllVisibleChallenges();
    ChallengeDetailResponse getChallenge(String loginId, Long challengeId);
    void create(ChallengeCreateRequest request, Long memberId);
    
    // 기존 메서드 (삭제 예정)
    @Deprecated
    Challenge join (Long memberId, Long id);
    
    // 새로운 PaymentFacade용 메서드들
    void validateJoinable(Long challengeId, Long userId);
    String createPendingUserChallenge(Long challengeId, Long userId);
    void completeUserChallenge(String uuid);
    void updateFailUserChallenge(String uuid);
    RequestPayDto getPaymentInfo(Long challengeId, Long userId);
}
