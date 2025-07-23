package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.common.entity.UserChallenge;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {

    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.challenge.id = :challengeId")
    long countByChallengeId(@Param("challengeId") Long challengeId);

    boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);
}
