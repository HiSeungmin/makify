package com.xladmt.makify.challenge.service;


import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.common.entity.Challenge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;

    public List<Challenge> getAllVisibleChallenges() {
        return challengeRepository.findAll()
                .stream()
                .filter(challenge -> challenge.getIsVisible().name().equals("Y"))
                .toList();
    }


    public void create(ChallengeCreateRequest request) {

    }
}
