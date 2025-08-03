package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.common.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    Optional<UserChallenge> findByUuid(String uuid);

    long countByChallengeId(Long challengeId);

    boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);

    Optional<UserChallenge> findByChallengeIdAndMemberId(Long challengeId, Long id);
}
