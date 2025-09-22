package com.xladmt.makify.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentInitRequest {
    private Long challengeId;
    private Long userId;
    private BigDecimal paymentAmount; // 결제 금액
}
