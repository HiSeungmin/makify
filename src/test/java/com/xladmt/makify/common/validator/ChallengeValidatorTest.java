package com.xladmt.makify.common.validator;

import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.constant.*;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.entity.VerificationMethod;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.willThrow;
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
    private Challenge challenge;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        member = Member.create("user1", "1234", Role.USER, "홍길동", "동동이"
                ,"gildong@test.com",LocalDate.now(),"010-1234-2345");

        verificationMethod = VerificationMethod.create(Frequency.DAILY, LocalTime.MIN, LocalTime.MAX, 1, VerificationType.CAMERA, YN.N);

        challenge = Challenge.create(member, "독서 챌린지", "매일 5쪽 읽기", LocalDate.now().minusDays(1), LocalDate.MAX, YN.Y, YN.N, BigDecimal.valueOf(1000),
                verificationMethod, null, 10, Category.MINDSET, null);

        challengeValidator = new ChallengeValidator(challengeRepository, memberRepository, userChallengeRepository);
    }

    @Test
    @DisplayName("챌리지 참여 가능성 종합 검증")
    public void validateJoinableTest(){

        // when
        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(userChallengeRepository.existsJoinedByChallengeIdAndMemberId(challenge.getId(), member.getId())).thenReturn(false);
        when(userChallengeRepository.countByChallengeId(challenge.getId())).thenReturn(0L);

        // then
        assertThatCode(() -> challengeValidator.validateJoinable(challenge.getId(), member.getId()))
                .doesNotThrowAnyException();
    }


    /**
     * 챌린지 존재 검증
     */
    @Test
    @DisplayName("챌리지 존재하지 않으면 CHALLENGE_NOT_FOUND 발생")
    public void existChallengeTest(){

        // given
        Long challengeId = 999L;
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> challengeValidator.validateChallengeExists(challengeId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CHALLENGE_NOT_FOUND.getMessage());

    }

    /**
     * 참여자 존재 검증
     * - 사용자 상태에 따른 존재 검증
     */
    @Test
    @DisplayName("참여자 존재 확인 검증")
    public void existMemberTest(){
        // given
        Long userId = 999L;
        when(challengeRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> challengeValidator.validateMemberExists(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    /**
     *  중복 참여 검증
     */
    @Test
    @DisplayName("챌리지 중복 참여 시, ALREADY_JOINED_CHALLENGE 발생")
    public void alreadyJoinedTest(){
        // given
        Long challengeId = 1L;
        Long userId = 100L;

        // when
        when(userChallengeRepository.existsJoinedByChallengeIdAndMemberId(challengeId, userId))
                .thenReturn(true); // 이미 참여한 것으로 설정

        // then
        assertThatThrownBy(() -> challengeValidator.validateNotAlreadyJoined(challengeId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_JOINED_CHALLENGE.getMessage());
    }

    /**
     *  참여 인원 제한 검증
     */
    @Test
    @DisplayName("챌리지 참여 인원 제한 검증")
    public void limitPersonTest(){

        // when
        when(userChallengeRepository.countByChallengeId(challenge.getId()))
                .thenReturn(10L); // 현재 인원이 최대 인원과 같음

        // when & then
        assertThatThrownBy(() -> challengeValidator.validateChallengeCapacity(challenge))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CHALLENGE_FULL.getMessage());

    }

    /**
     * 챌린지 시작일 검증
     */
    @Test
    @DisplayName("챌리지 시작일 검증")
    public void startDateTest(){
        // 챌린지 어제 시작

        // when & then
        assertThatThrownBy(() -> challengeValidator.validateChallengeStartDate(challenge))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CHALLENGE_ALREADY_STARTED.getMessage());
    }

}
