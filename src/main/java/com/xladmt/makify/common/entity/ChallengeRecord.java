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

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    private LocalDateTime verificatedDate;

    @Enumerated(EnumType.STRING)
    private YN is_approved;

    @Column(name = "refusal_reason", length = 100)
    private String refusalReason;

    @Enumerated(EnumType.STRING)
    private YN isVisible;

    private Long manager;
}
