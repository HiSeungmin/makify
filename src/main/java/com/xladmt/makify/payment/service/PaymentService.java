package com.xladmt.makify.payment.service;


import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.xladmt.makify.payment.dto.PaymentCallbackRequest;
import com.xladmt.makify.payment.dto.RequestPayDto;

public interface PaymentService {
    IamportResponse<Payment> verifyExternalPayment(String paymentUid);
    void completePayment(String uuid, String paymentUid);
    void failPayment(String uuid, String paymentUid);
    void cancelExternalPayment(String paymentUid);
}
