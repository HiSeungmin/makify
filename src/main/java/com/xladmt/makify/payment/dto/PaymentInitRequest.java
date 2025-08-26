package com.xladmt.makify.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentInitRequest {
    private Long challengeId;
    private Long userId;
    private Long paymentAmount; // 결제 금액
}
