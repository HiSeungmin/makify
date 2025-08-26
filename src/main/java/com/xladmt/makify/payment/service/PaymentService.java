package com.xladmt.makify.payment.service;


import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.xladmt.makify.payment.dto.PaymentCallbackRequest;
import com.xladmt.makify.payment.dto.RequestPayDto;

public interface PaymentService {
    // 기존 메서드 (Facade로 이동 예정)
    @Deprecated
    IamportResponse<Payment> paymentByCallback(PaymentCallbackRequest request);
    
    // 새로운 PaymentFacade용 메서드들
    IamportResponse<Payment> verifyExternalPayment(String paymentUid);
    void validatePaymentAmount(String uuid, Payment externalPayment);
    void completePayment(String uuid, String paymentUid);
    void cancelExternalPayment(String paymentUid);
}
