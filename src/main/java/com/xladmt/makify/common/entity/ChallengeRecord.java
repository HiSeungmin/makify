package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.YN;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ChallengeRecord")
public class ChallengeRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "verificated_date", nullable = false)
    private LocalDateTime verificatedDate;

    @Enumerated(EnumType.STRING)
    private YN is_approved;

    @Column(name = "refusal_reason", length = 100)
    private String refusalReason;

    @Enumerated(EnumType.STRING)
    private YN isVisible;

    private Long manager;
}
