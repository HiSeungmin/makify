package com.xladmt.makify.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackRequest {
    @JsonProperty("paymentUid") // JavaScript에서 보내는 이름과 일치
    private String paymentUid; // 결제 고유 번호

    private String uuid; // 주문 고유 번호

}
