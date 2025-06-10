package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.common.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    // 필요한 경우 쿼리 메서드 추가 가능
}
