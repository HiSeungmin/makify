package com.xladmt.makify.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String startDate;
    private String endDate;
    private Integer targetFrequency;
    private Integer actualFrequency;
    private String alarmTime;
    private String alarmDate;
    private String status;
    private Integer deAmt;
}
