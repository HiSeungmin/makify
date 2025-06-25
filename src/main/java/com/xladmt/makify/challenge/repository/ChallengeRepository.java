package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.common.entity.Challenge;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    @Query("SELECT c FROM Challenge c JOIN FETCH c.member WHERE c.id = :id")
    Optional<Challenge> findByIdWithMember(@Param("id") Long id);
}
