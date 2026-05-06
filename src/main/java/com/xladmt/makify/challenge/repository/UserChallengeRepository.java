package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.challenge.domain.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

    @Query("SELECT uc " +
            "FROM UserChallenge uc " +
            "WHERE uc.member.id = :memberId " +
            "AND uc.challenge.id = :challengeId " +
            "AND uc.status = 'JOINED'")
    Optional<UserChallenge> findByMemberIdAndChallengeId(@Param("memberId") Long memberId,
                                                         @Param("challengeId") Long challengeId);

    @Query("SELECT uc " +
            "FROM UserChallenge uc " +
            "JOIN FETCH uc.challenge " +
            "WHERE uc.member.id = :memberId " +
            "AND uc.status IN ('JOINED', 'COMPLETED', 'FAIL')")
    List<UserChallenge> findByMemberId(@Param("memberId") Long memberId);

    /**
     * [시작 알림용] 오늘 시작하는 챌린지의 참여자 중,
     * vm.startTime 이 지정된 시각과 일치하는 JOINED 상태 UserChallenge 목록.
     */
    @Query("SELECT uc FROM UserChallenge uc " +
            "JOIN FETCH uc.challenge c " +
            "JOIN FETCH c.verificationMethod vm " +
            "JOIN FETCH uc.member m " +
            "WHERE c.startDate = :today " +
            "AND vm.startTime = :targetStartTime " +
            "AND uc.status = 'JOINED'")
    List<UserChallenge> findJoinedForChallengeStart(@Param("today") LocalDate today,
                                                    @Param("targetStartTime") LocalTime targetStartTime);

    /**
     * [리마인더용] 오늘이 챌린지 기간 내이고,
     * vm.startTime 이 지정된 시각과 일치하는 JOINED 상태 UserChallenge 목록.
     * Frequency 분기는 서비스 레이어에서 처리한다.
     */
    @Query("SELECT uc FROM UserChallenge uc " +
            "JOIN FETCH uc.challenge c " +
            "JOIN FETCH c.verificationMethod vm " +
            "JOIN FETCH uc.member m " +
            "WHERE c.startDate <= :today " +
            "AND c.endDate >= :today " +
            "AND vm.startTime = :targetStartTime " +
            "AND uc.status = 'JOINED'")
    List<UserChallenge> findJoinedForReminder(@Param("today") LocalDate today,
                                               @Param("targetStartTime") LocalTime targetStartTime);

    /**
     * [종료 판정용] 지정된 날짜에 endDate가 걸린 챌린지의 JOINED 상태 UserChallenge 목록.
     * 아직 판정되지 않은(JOINED) 참여자만 대상으로 하여, 재실행 시 중복 처리를 방지한다.
     */
    @Query("SELECT uc FROM UserChallenge uc " +
            "JOIN FETCH uc.challenge c " +
            "JOIN FETCH uc.member m " +
            "WHERE c.endDate = :endedOn " +
            "AND uc.status = 'JOINED'")
    List<UserChallenge> findJoinedForFinalization(@Param("endedOn") LocalDate endedOn);
}
