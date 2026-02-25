package com.xladmt.makify.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitRequest {
    private Long challengeId;
    private Long userId;
    private BigDecimal paymentAmount; // 결제 금액
}
