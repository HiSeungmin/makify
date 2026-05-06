package com.xladmt.makify.payment.domain;

import com.xladmt.makify.common.constant.RefundStatus;
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
@Table(name = "Refund")
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    private BigDecimal amount; // 환불 금액

    private String reason; // 환불 사유

    @Enumerated(EnumType.STRING)
    private RefundStatus status; // 환불 상태

    private String stripePayoutId; // 환급 ID

    private LocalDateTime refundDate;
}
