package com.xladmt.makify.verification.repository;

import com.xladmt.makify.common.entity.ChallengeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ChallengeRecordRepository extends JpaRepository<ChallengeRecord, Long> {

    @Query("SELECT COUNT(cr) FROM ChallengeRecord cr " +
            "WHERE cr.member.id = :memberId " +
            "AND cr.challenge.id = :challengeId " +
            "AND cr.isVisible = 'Y' " +
            "AND FUNCTION('DATE', cr.verificatedDate) = :today")
    int countTodayVerifications(@Param("memberId") Long memberId,
                                @Param("challengeId") Long challengeId,
                                @Param("today") LocalDate today);

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
}
