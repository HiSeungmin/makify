package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.UserChallengeStatus;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    private String uuid; // 고유 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private Double progress;

    private String startDate; // 참여 날짜
    private String endDate; // 완료 날짜

    private Integer targetFrequency; // 목표 수행 횟수
    private Integer actualFrequency; // 실제 수행 횟수

    private String alarmTime; // 알림 설정 시간
    private String alarmDate; // 알림 반복 요일

    @Enumerated(EnumType.STRING)
    private UserChallengeStatus status;

    private Integer deAmt; // 예치금


    public static UserChallenge createUserChallenge(
            Challenge challenge,
            Member member,
            Payment payment,
            String uuid
    ) {
        UserChallenge userChallenge = new UserChallenge();
        userChallenge.challenge = challenge;
        userChallenge.member = member;
        userChallenge.payment = payment;
        userChallenge.uuid = uuid;
        userChallenge.status = UserChallengeStatus.PENDING; // 초기 상태는 PENDING

        return userChallenge;
    }

    // 챌린지 참여 상태를 JOINED로 변경
    public void markAsJoined() {
        this.status = UserChallengeStatus.JOINED;
    }

    // 챌린지 참여 상태를 FAILED로 변경
    public void markAsFailed() {
        this.status = UserChallengeStatus.FAIL;
    }

    // 챌린지 참여 상태를 취소로 변경
    public void markAsCancelled() {
        this.status = UserChallengeStatus.CANCEL;
    }


}
