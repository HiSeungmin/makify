package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.PaidStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Payment")
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Member user;

    private String stripePaymentId; // 네이버페이, 카카오페이 결제 ID

    private Integer amount; // 결제 금액
    private Integer depositAmt; // 예치금 사용 금액

    @Enumerated(EnumType.STRING)
    private PaidStatus status; // 결제 상태

    private LocalDateTime paymentDate; // 결제 날짜
}
