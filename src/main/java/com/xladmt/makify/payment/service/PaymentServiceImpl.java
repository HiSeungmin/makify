package com.xladmt.makify.payment.service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.constant.PaidStatus;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserChallengeRepository userChallengeRepository;
    private final PaymentRepository paymentRepository;
    private final IamportClient iamportClient;


    @Override
    public IamportResponse<Payment> verifyExternalPayment(String paymentUid) {
        try {
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(paymentUid);
            
            // 결제 완료 상태 확인
            if (!iamportResponse.getResponse().getStatus().equals("paid")) {
                throw new RuntimeException("결제가 완료되지 않았습니다.");
            }
            
            return iamportResponse;
            
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException("외부 결제 검증 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void completePayment(String uuid, String paymentUid) {
        UserChallenge userChallenge = userChallengeRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보가 존재하지 않습니다."));
        
        // 결제 상태를 COMPLETE로 변경하고 paymentUid 저장
        userChallenge.getPayment().updateStatus(PaidStatus.COMPLETE, paymentUid);
    }

    @Override
    public void failPayment(String uuid, String paymentUid) {
        UserChallenge userChallenge = userChallengeRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보가 존재하지 않습니다."));

        // 결제 상태를 Fail로 변경
        userChallenge.getPayment().updateStatus(PaidStatus.FAIL, paymentUid);
    }

    @Override
    public void cancelExternalPayment(String paymentUid) {
        try {
            // 외부 결제 취소 요청
            CancelData cancelData = new CancelData(paymentUid, true);
            iamportClient.cancelPaymentByImpUid(cancelData);
            
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException("외부 결제 취소 실패: " + e.getMessage(), e);
        }
    }
}
