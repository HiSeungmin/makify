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

        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (request.getChallengeId() == null || request.getUserId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 1. 챌린지 참여 가능 여부 검증
        challengeValidator.validateJoinable(request.getChallengeId(), request.getUserId());

        // 2. PENDING 상태로 UserChallenge + Payment 생성
        String uuid = challengeService.createPendingUserChallenge(request.getChallengeId(), request.getUserId());

        return uuid;
    }

    /**
     * 결제 완료 처리 - 외부 결제 API 콜백에서 호출
     * 결제 검증 후 상태를 COMPLETE로 변경
     */
    @Transactional
    public IamportResponse<Payment> processPaymentCallback(PaymentCallbackRequest request) {
        try {
            // imp_uid가 없으면 프론트에서 결제 자체가 실패한 것 → DB만 정리
            if (request.getPaymentUid() == null || request.getPaymentUid().isBlank()) {
                challengeService.failUserChallenge(request.getUuid());
                paymentService.failPayment(request.getUuid(), null);
                throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
            }

            // 1. 외부 결제 검증
            IamportResponse<Payment> iamportResponse = paymentService.verifyExternalPayment(request.getPaymentUid());

            if (iamportResponse == null || iamportResponse.getResponse() == null) {
                throw new BusinessException(ErrorCode.IAMPORT_RESPONSE_ERROR);
            }

            // 2. DB의 결제 정보와 대조 검증
            paymentValidator.validatePaymentAmount(request.getUuid(), iamportResponse.getResponse());

            // 3. 상태 변경: PENDING → COMPLETE
            challengeService.completeUserChallenge(request.getUuid());
            paymentService.completePayment(request.getUuid(), request.getPaymentUid());

            return iamportResponse;

        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.PAYMENT_NOT_COMPLETED) {
                // imp_uid 없음 → 이미 위에서 DB 정리 완료, 아무것도 하지 않음
            } else if (e.getErrorCode() == ErrorCode.IAMPORT_RESPONSE_ERROR) {
                // 아임포트 조회 실패 → 실제 결제 안 됨, DB만 정리
                challengeService.failUserChallenge(request.getUuid());
                paymentService.failPayment(request.getUuid(), request.getPaymentUid());
            } else {
                // 결제는 됐지만 검증 실패(금액 불일치 등) → 외부 취소까지
                cleanupFailedPayment(request.getUuid(), request.getPaymentUid());
            }
            throw e;
        }
    }

    /**
     * 실패한 결제 처리
     */
    private void cleanupFailedPayment(String uuid, String paymentUid) {

        if (paymentUid == null || paymentUid.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 1. DB에서 PENDING -> CANCEL
        challengeService.failUserChallenge(uuid);
        paymentService.failPayment(uuid, paymentUid);

        // 2. 외부 결제 취소
        paymentService.cancelExternalPayment(paymentUid);
    }

}
