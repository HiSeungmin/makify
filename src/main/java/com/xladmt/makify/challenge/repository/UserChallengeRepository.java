package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.common.constant.UserChallengeStatus;
import com.xladmt.makify.common.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    Optional<UserChallenge> findByUuid(String uuid);

    long countByChallengeId(Long challengeId);

    @Query("SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END " +
            "FROM UserChallenge uc " +
            "WHERE uc.challenge.id = :challengeId " +
            "AND uc.member.id = :memberId " +
            "AND uc.status = 'JOINED'")
    boolean existsJoinedByChallengeIdAndMemberId(@Param("challengeId") Long challengeId,
                                                 @Param("memberId") Long memberId);
    Optional<UserChallenge> findByChallengeIdAndMemberId(
            Long challengeId,
            Long memberId
    );
}
