package com.xladmt.makify.challenge.service;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.VerificationMethodRepository;
import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.Role;
import com.xladmt.makify.common.constant.VerificationType;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.entity.VerificationMethod;
import com.xladmt.makify.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceImplTest {
//    @Mock private ChallengeRepository challengeRepository;
//    @InjectMocks private ChallengeServiceImpl challengeService;

    @Autowired private ChallengeServiceImpl challengeService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ChallengeRepository challengeRepository;
    @Autowired private VerificationMethodRepository verificationMethodRepository;

    private Member member;

    @BeforeEach
    void init(){
        member = Member.create("test", "password", Role.USER, "테스트","테스트"
        ,"test@test.com", LocalDate.from(LocalDateTime.now()),"010-1111-1111");
        memberRepository.save(member);

    }


    @Test
    void getAllVisibleChallengesTest() {
        // given
        Member dummyMember = mock(Member.class);
        VerificationMethod dummyMethod = mock(VerificationMethod.class);

        Challenge visible1 = Challenge.create(dummyMember, "공개 챌린지 1", "설명", LocalDate.now(), LocalDate.now().plusDays(10),
                YN.Y, YN.N, 1000, dummyMethod, null, 10, Category.EXERCISE);
        Challenge visible2 = Challenge.create(dummyMember, "공개 챌린지 2", "설명", LocalDate.now(), LocalDate.now().plusDays(10),
                YN.Y, YN.Y, 2000, dummyMethod, null, 15, Category.STUDY);
        Challenge hidden = Challenge.create(dummyMember, "숨김 챌린지", "설명", LocalDate.now(), LocalDate.now().plusDays(10),
                YN.Y, YN.N, 500, dummyMethod, null, 5, Category.HOBBY);

        hidden.delete();

        when(challengeRepository.findAll()).thenReturn(List.of(visible1, visible2, hidden));

        // when
        List<Challenge> result = challengeService.getAllVisibleChallenges();

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains(visible1));
        assertTrue(result.contains(visible2));
        assertFalse(result.contains(hidden));
    }



}