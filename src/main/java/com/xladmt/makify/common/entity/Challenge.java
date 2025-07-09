package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.ChallengeStatus;
import com.xladmt.makify.common.constant.YN;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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
    private Member member;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", nullable = false, length = 100)
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private YN isPublic; // 공개 여부

    @Enumerated(EnumType.STRING)
    private YN isFixedDeposit; // 고정 예치금 유무

    private Integer maxDeposit;  // 예치금

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "verification_id", nullable = false)
    private VerificationMethod verificationMethod; // 인증 방법 ID

    private String privateCode; // 비공개 참여 코드

    @Column(name = "max_Participants", nullable = false)
    private Integer maxParticipants; // 최대 모집 인원

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20)
    private Category category;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    @Enumerated(EnumType.STRING)
    private YN isVisible;

    // 생성 메서드
    public static Challenge create(Member member,
                                   String title,
                                   String description,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   YN isPublic,
                                   YN isFixedDeposit,
                                   Integer maxDeposit,
                                   VerificationMethod verificationMethod,
                                   String privateCode,
                                   Integer maxParticipants,
                                   Category category) {
        Challenge challenge = new Challenge();
        challenge.member = member;
        challenge.title = title;
        challenge.description = description;
        challenge.startDate = startDate;
        challenge.endDate = endDate;
        challenge.isPublic = isPublic;
        challenge.isFixedDeposit = isFixedDeposit;
        challenge.maxDeposit = maxDeposit;
        challenge.verificationMethod = verificationMethod;
        challenge.privateCode = Objects.nonNull(privateCode)?privateCode:null;
        challenge.maxParticipants = maxParticipants;
        challenge.category = category;
        challenge.status = ChallengeStatus.NOT_STARTED;
        challenge.isVisible = YN.Y;
        return challenge;
    }

    // 수정 메서드
    public void update(String title,
                       String description,
                       LocalDate startDate,
                       LocalDate endDate,
                       YN isPublic,
                       YN isFixedDeposit,
                       Integer maxDeposit,
                       VerificationMethod verificationMethod,
                       String privateCode,
                       Integer maxParticipants,
                       Category category) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isPublic = isPublic;
        this.isFixedDeposit = isFixedDeposit;
        this.maxDeposit = maxDeposit;
        this.verificationMethod = verificationMethod;
        this.privateCode = privateCode;
        this.maxParticipants = maxParticipants;
        this.category = category;
    }

    // 삭제 메서드
    public void delete() {
        this.isVisible = YN.N;
    }
}
