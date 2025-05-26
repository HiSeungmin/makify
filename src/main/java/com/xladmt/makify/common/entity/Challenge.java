package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.ChallengeStatus;
import com.xladmt.makify.common.constant.YN;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Challenge")
public class Challenge extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member memberId;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", nullable = false, length = 100)
    private String description;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private YN isPublic; // 공개 여부

    @Enumerated(EnumType.STRING)
    private YN isFixedDeposit; // 고정 예치금 유무

    private Integer maxDeposit;  // 예치금

    @OneToOne
    @JoinColumn(name = "verification_id", nullable = false)
    private VerificationMethod verificationId; // 인증 방법 ID

    private String privateCode; // 비공개 참여 코드

    @Column(name = "max_Participants", nullable = false)
    private Integer maxParticipants; // 최대 모집 인원

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    @Enumerated(EnumType.STRING)
    private YN isVisible;
}
