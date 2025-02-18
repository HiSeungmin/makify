package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.PayoutStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "MoneyBack")
public class Payout extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment paymentId;

    private Integer amount; // 환급 금액

    @Enumerated(EnumType.STRING)
    private PayoutStatus status; // 환급 상태

    private String stripePayoutId; // 환급 ID
    private LocalDateTime payoutDate; // 환급 날짜
}
