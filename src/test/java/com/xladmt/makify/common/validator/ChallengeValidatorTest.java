package com.xladmt.makify.common.validator;

import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.constant.*;
import com.xladmt.makify.challenge.domain.Challenge;
import com.xladmt.makify.member.domain.Member;
import com.xladmt.makify.verification.domain.VerificationMethod;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ChallengeValidatorTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserChallengeRepository userChallengeRepository;

    @InjectMocks
    private ChallengeValidator challengeValidator;

    private Member member;
    private VerificationMethod verificationMethod;

    // startDate = 어제 (시작일 검증 실패 케이스용)
    private Challenge startedChallenge;

    // startDate = 내일 (정상 참여 가능 케이스용)
    private Challenge upcomingChallenge;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        member = Member.create("user1", "1234", Role.USER, "홍길동", "동동이",
                "gildong@test.com", LocalDate.now(), "010-1234-2345");

        verificationMethod = VerificationMethod.create(
                Frequency.DAILY, LocalTime.MIN, LocalTime.MAX, 1, VerificationType.CAMERA, YN.N);

        // 이미 시작된 챌린지 (startDate = 어제)
        startedChallenge = Challenge.create(member, "독서 챌린지", "매일 5쪽 읽기",
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                YN.Y, YN.N, BigDecimal.valueOf(1000),
                verificationMethod, null, 10, Category.MINDSET, null);

        // 아직 시작 안 된 챌린지 (startDate = 내일)
        upcomingChallenge = Challenge.create(member, "운동 챌린지", "매일 30분 운동",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                YN.Y, YN.N, BigDecimal.valueOf(1000),
                verificationMethod, null, 10, Category.EXERCISE, null);

        challengeValidator = new ChallengeValidator(challengeRepository, memberRepository, userChallengeRepository);
    }

    @Test
    @DisplayName("챌린지 참여 가능성 종합 검증")
    public void validateJoinableTest() {
        // given - startDate가 내일인 upcomingChallenge 사용
        when(challengeRepository.findById(upcomingChallenge.getId())).thenReturn(Optional.of(upcomingChallenge));
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(userChallengeRepository.existsJoinedByChallengeIdAndMemberId(upcomingChallenge.getId(), member.getId())).thenReturn(false);
        when(userChallengeRepository.countByChallengeId(upcomingChallenge.getId())).thenReturn(0L);

        // then
        assertThatCode(() -> challengeValidator.validateJoinable(upcomingChallenge.getId(), member.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("챌린지 존재하지 않으면 CHALLENGE_NOT_FOUND 발생")
    public void existChallengeTest() {
        Long challengeId = 999L;
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> challengeValidator.validateChallengeExists(challengeId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CHALLENGE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("참여자 존재 확인 검증")
    public void existMemberTest() {
        Long userId = 999L;
        when(memberRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> challengeValidator.validateMemberExists(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("챌린지 중복 참여 시, ALREADY_JOINED_CHALLENGE 발생")
    public void alreadyJoinedTest() {
        Long challengeId = 1L;
        Long userId = 100L;
        when(userChallengeRepository.existsJoinedByChallengeIdAndMemberId(challengeId, userId)).thenReturn(true);

        assertThatThrownBy(() -> challengeValidator.validateNotAlreadyJoined(challengeId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_JOINED_CHALLENGE.getMessage());
    }

    @Test
    @DisplayName("챌린지 참여 인원 제한 검증")
    public void limitPersonTest() {
        when(userChallengeRepository.countByChallengeId(upcomingChallenge.getId())).thenReturn(10L);

        assertThatThrownBy(() -> challengeValidator.validateChallengeCapacity(upcomingChallenge))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CHALLENGE_FULL.getMessage());
    }

    @Test
    @DisplayName("챌린지 시작일 검증 - 이미 시작된 경우 예외 발생")
    public void startDateTest() {
        assertThatThrownBy(() -> challengeValidator.validateChallengeStartDate(startedChallenge))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CHALLENGE_ALREADY_STARTED.getMessage());
    }
}
