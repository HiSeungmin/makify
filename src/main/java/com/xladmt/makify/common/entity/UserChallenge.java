package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.Sledding;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private Double progress;

    private String startDate; // 참여 날짜
    private String endDate; // 완료 날짜

    private Integer targetFrequency; // 목표 수행 횟수
    private Integer actualFrequency; // 실제 수행 횟수

    private String alarmTime; // 알림 설정 시간
    private String alarmDate; // 알림 반복 요일

    @Enumerated(EnumType.STRING)
    private Sledding status;

    private Integer deAmt; // 예치금
}
