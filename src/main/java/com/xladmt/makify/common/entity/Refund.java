package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.RefundStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Refund") // 환불
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment paymentId; // 결제 ID

    private Integer amount; // 환불금액
    private Integer depositAmt; //

    @Enumerated(EnumType.STRING)
    private RefundStatus status; // 환불 상태

    private LocalDateTime refundDate;
}
