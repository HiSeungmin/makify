package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.entity.Challenge;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    
    @Query("SELECT c FROM Challenge c JOIN FETCH c.member WHERE c.id = :id")
    Optional<Challenge> findByIdWithMember(@Param("id") Long id);
    
    /**
     * 챌린지 검색 (제목, 설명 키워드 + 카테고리 필터)
     */
    @Query("SELECT c FROM Challenge c JOIN FETCH c.member " +
           "WHERE (:keyword IS NULL OR " +
           "       LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR c.category = :category) " +
           "AND c.isVisible = 'Y' ")
    Page<Challenge> searchChallenges(
        @Param("keyword") String keyword,
        @Param("category") Category category,
        Pageable pageable
    );
}
