package com.xladmt.makify.challenge.service;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.common.entity.Challenge;

import java.util.List;

public interface ChallengeService {
    List<Challenge> getAllVisibleChallenges();
    void create(ChallengeCreateRequest request);
}
