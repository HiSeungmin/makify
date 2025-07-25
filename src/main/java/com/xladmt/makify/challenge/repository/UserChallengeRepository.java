package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.common.entity.UserChallenge;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {

    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.challenge.id = :challengeId")
    long countByChallengeId(@Param("challengeId") Long challengeId);

    boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);

    @Query("select o from UserChallenge o" +
            " left join fetch o.challenge c" +
            " left join fetch o.member m" +
            " where o.uuid = :uuid")
    Optional<UserChallenge> findByUuid(String uuid);


    @Query("select o from UserChallenge o" +
            " left join fetch o.payment p" +
            " where o.uuid = :uuid")
    Optional<UserChallenge> findPayment(String uuid);

    Optional<UserChallenge> findByChallengeIdAndMemberId(Long challengeId, Long memberId);
}
