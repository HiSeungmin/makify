package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.ChallengeStatus;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.challenge.domain.Challenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
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

    List<Challenge> findAllByIsVisibleOrderByCreatedAtDesc(YN isVisible);

    @Query("SELECT c FROM Challenge c WHERE c.id IN :ids AND c.isVisible = 'Y'")
            List<Challenge> findHomeChallenge(@Param("ids") List<Long> ids);

    /**
     * 오늘 시작하는 챌린지들을 IN_PROGRESS 로 일괄 갱신.
     * Challenge.status 컬럼은 시작/종료 스케줄러가 관리한다.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Challenge c SET c.status = :status " +
            "WHERE c.startDate = :today AND c.status = 'NOT_STARTED'")
    int bulkUpdateStatusOnStart(@Param("today") LocalDate today,
                                 @Param("status") ChallengeStatus status);

    /**
     * 지정된 날짜에 endDate가 걸린 챌린지들을 COMPLETED 로 일괄 갱신.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Challenge c SET c.status = :status " +
            "WHERE c.endDate = :endedOn AND c.status <> 'COMPLETED'")
    int bulkUpdateStatusOnEnd(@Param("endedOn") LocalDate endedOn,
                               @Param("status") ChallengeStatus status);
}
