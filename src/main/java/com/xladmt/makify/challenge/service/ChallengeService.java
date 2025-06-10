package com.xladmt.makify.challenge.service;


import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.common.entity.Challenge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public List<Challenge> getAllVisibleChallenges() {
        return challengeRepository.findAll()
                .stream()
                .filter(challenge -> challenge.getIsVisible().name().equals("Y"))
                .toList();
    }

    public Challenge getChallengeById(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다. id = " + id));
    }

    // TODO: 검색 조건 등 추가 구현
}
