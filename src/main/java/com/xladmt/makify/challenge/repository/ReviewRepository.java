package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.common.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByMemberIdAndChallengeId(Long memberId, Long challengeId);
    Optional<Review> findByMemberIdAndChallengeId(Long memberId, Long challengeId);

    @Query("SELECT r FROM Review r JOIN FETCH r.challenge WHERE r.member.id = :memberId ORDER BY r.createDate DESC")
    List<Review> findByMemberIdWithChallenge(@Param("memberId") Long memberId);
}
