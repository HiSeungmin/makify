package com.xladmt.makify.verification.repository;

import com.xladmt.makify.common.entity.ChallengeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChallengeRecordRepository extends JpaRepository<ChallengeRecord, Long> {

    @Query("SELECT COUNT(cr) FROM ChallengeRecord cr " +
            "WHERE cr.member.id = :memberId " +
            "AND cr.challenge.id = :challengeId " +
            "AND cr.isVisible = 'Y' " +
            "AND FUNCTION('DATE', cr.verificatedDate) = :today")
    int countTodayVerifications(@Param("memberId") Long memberId,
                                @Param("challengeId") Long challengeId,
                                @Param("today") LocalDate today);

    /**
     * 주간 인증 횟수 (월요일 00:00 ~ 일요일 23:59:59.999 범위).
     * Frequency = N_PER_WEEK 리마인더 판정에 사용.
     */
    @Query("SELECT COUNT(cr) FROM ChallengeRecord cr " +
            "WHERE cr.member.id = :memberId " +
            "AND cr.challenge.id = :challengeId " +
            "AND cr.isVisible = 'Y' " +
            "AND cr.isApproved = 'Y' " +
            "AND cr.verificatedDate >= :weekStart " +
            "AND cr.verificatedDate < :weekEndExclusive")
    int countThisWeekVerifications(@Param("memberId") Long memberId,
                                    @Param("challengeId") Long challengeId,
                                    @Param("weekStart") LocalDateTime weekStart,
                                    @Param("weekEndExclusive") LocalDateTime weekEndExclusive);

    @Query("""
    SELECT COUNT(cr) FROM ChallengeRecord cr
    WHERE cr.member.id = :memberId
      AND cr.challenge.id = :challengeId  
      AND cr.isVisible = 'Y'
      AND cr.isApproved = 'Y'
      AND cr.verificatedDate >= :weekStart
      AND cr.verificatedDate < :weekEnd
""")
    long countInWeek(@Param("memberId") Long memberId,
                     @Param("challengeId") Long challengeId,
                     @Param("weekStart") LocalDateTime weekStart,
                     @Param("weekEnd") LocalDateTime weekEnd);


    @Query("SELECT cr FROM ChallengeRecord cr " +
            "WHERE cr.member.id = :memberId " +
            "AND cr.challenge.id = :challengeId " +
            "AND cr.isVisible = 'Y' " +
            "ORDER BY cr.verificatedDate DESC")
    List<ChallengeRecord> findAllByMemberAndChallenge(@Param("memberId") Long memberId,
                                                       @Param("challengeId") Long challengeId);

    @Query("SELECT cr FROM ChallengeRecord cr " +
            "WHERE cr.member.id != :memberId " +
            "AND cr.challenge.id = :challengeId " +
            "AND cr.isVisible = 'Y' " +
            "ORDER BY cr.verificatedDate DESC")
    List<ChallengeRecord> findAllByOtherMembers(@Param("memberId") Long memberId,
                                                @Param("challengeId") Long challengeId);

    @Query("SELECT COUNT(cr) FROM ChallengeRecord cr " +
            "WHERE cr.member.id = :memberId " +
            "AND cr.challenge.id = :challengeId " +
            "AND cr.isVisible = 'Y'")
    int countAllVerifications(@Param("memberId") Long memberId,
                              @Param("challengeId") Long challengeId);

    ChallengeRecord findByIdAndMemberId(Long id, Long memberId);

    @Query("SELECT cr FROM ChallengeRecord cr JOIN FETCH cr.member WHERE cr.id = :id")
    Optional<ChallengeRecord> findByIdWithMember(@Param("id") Long id);
}
