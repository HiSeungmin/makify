package com.xladmt.makify.payment.domain;

import com.xladmt.makify.common.constant.PayoutStatus;
import com.xladmt.makify.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "MoneyBack") // 환급
public class Payout extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment paymentId;

    private BigDecimal amount; // 환급 금액

    @Enumerated(EnumType.STRING)
    private PayoutStatus status; // 환급 상태

    private String stripePayoutId; // 간편결제 고유 ID
    private LocalDateTime payoutDate; // 환급 날짜
}
