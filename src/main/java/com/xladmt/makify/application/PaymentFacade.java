package com.xladmt.makify.application;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.xladmt.makify.challenge.service.ChallengeService;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.common.validator.ChallengeValidator;
import com.xladmt.makify.common.validator.PaymentValidator;
import com.xladmt.makify.payment.dto.PaymentCallbackRequest;
import com.xladmt.makify.payment.dto.PaymentInitRequest;
import com.xladmt.makify.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {
    private final ChallengeService challengeService;
    private final PaymentService paymentService;
    private final ChallengeValidator challengeValidator;
    private final PaymentValidator paymentValidator;

    /**
     * 결제 초기화 - "결제하기" 버튼 클릭 시 호출
     * PENDING 상태로 UserChallenge, Payment 생성
     */
    @Transactional
    public String initializePayment(PaymentInitRequest request) {

        try {
            // 1. 챌린지 참여 가능 여부 검증
            challengeValidator.validateJoinable(request.getChallengeId(), request.getUserId());
            
            // 2. PENDING 상태로 UserChallenge + Payment 생성
            String uuid = challengeService.createPendingUserChallenge(request.getChallengeId(), request.getUserId());

            return uuid;
            
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PAYMENT_INIT_FAIL);
        }
    }

    /**
     * 결제 완료 처리 - 외부 결제 API 콜백에서 호출
     * 결제 검증 후 상태를 COMPLETE로 변경
     */
    @Transactional
    public IamportResponse<Payment> processPaymentCallback(PaymentCallbackRequest request) {
        try {
            // 1. 외부 결제 검증
            IamportResponse<Payment> iamportResponse = paymentService.verifyExternalPayment(request.getPaymentUid());
            
            // 2. DB의 결제 정보와 대조 검증
            paymentValidator.validatePaymentAmount(request.getUuid(), iamportResponse.getResponse());
            
            // 3. 상태 변경: PENDING → COMPLETE
            challengeService.completeUserChallenge(request.getUuid());
            paymentService.completePayment(request.getUuid(), request.getPaymentUid());

            return iamportResponse;
            
        } catch (Exception e) {
            cleanupFailedPayment(request.getUuid(), request.getPaymentUid());
            throw new BusinessException(ErrorCode.PAYMENT_PROCESSING_FAIL);
        }
    }

    /**
     * 실패한 결제 처리
     */
    private void cleanupFailedPayment(String uuid, String paymentUid) {
        try {
            // 1. DB에서 PENDING -> CANCEL
            challengeService.failUserChallenge(uuid);
            paymentService.failPayment(uuid, paymentUid);
            
            // 2. 외부 결제 취소
            if (paymentUid != null && !paymentUid.isEmpty()) {
                paymentService.cancelExternalPayment(paymentUid);
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FAIL);
        }
    }

    // TODO: 결제 취소(환불) 처리
}
