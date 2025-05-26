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
@Table(name = "UserChallenge")
public class UserChallenge extends BaseEntity {
    @Id
    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @Id
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private Double progress;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer targetFrequency; // 목표 수행 횟수
    private Integer actualFrequency; // 실제 수행 횟수

    @Enumerated(EnumType.STRING)
    private YN isAlram;

    @Enumerated(EnumType.STRING)
    private YN isActive;
}
