package com.xladmt.makify.challenge.service;

import com.xladmt.makify.challenge.dto.*;
import com.xladmt.makify.challenge.domain.Challenge;
import com.xladmt.makify.payment.dto.RequestPayDto;

import java.util.List;

public interface ChallengeService {
    List<Challenge> getAllVisibleChallenges();
    ChallengePageDto getChallengesPage(List<Challenge> allChallenges, int page, int size);
    ChallengeDetailResponse getChallenge(String loginId, Long challengeId);
    void create(ChallengeCreateRequest request, Long memberId);
    ChallengeSearchResponse searchChallenges(ChallengeSearchRequest request);

    String createPendingUserChallenge(Long challengeId, Long userId);
    void completeUserChallenge(String uuid);
    void failUserChallenge(String uuid);
    RequestPayDto getPaymentInfo(Long challengeId, Long userId);
}
