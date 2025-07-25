package com.xladmt.makify.payment.service;


import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.xladmt.makify.payment.dto.PaymentCallbackRequest;
import com.xladmt.makify.payment.dto.RequestPayDto;

public interface PaymentService {
//     결제 요청 데이터 조회
    //RequestPayDto findRequestDto(String uuid);

//     결제(콜백)
    IamportResponse<Payment> paymentByCallback(PaymentCallbackRequest request);
}
