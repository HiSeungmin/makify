package com.xladmt.makify.challenge.service;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.dto.ChallengeDetailResponse;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.payment.dto.RequestPayDto;

import java.util.List;

public interface ChallengeService {
    List<Challenge> getAllVisibleChallenges();
    ChallengeDetailResponse getChallenge(String loginId, Long challengeId);
    void create(ChallengeCreateRequest request, Long memberId);
    //Challenge showChallenge (Long challengeId);
    Challenge join (Long memberId, Long id);
    RequestPayDto getRequestPayDto(Long challengeId, Long memberId);
}
