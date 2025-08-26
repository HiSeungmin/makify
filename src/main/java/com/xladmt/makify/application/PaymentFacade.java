package com.xladmt.makify.application;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.xladmt.makify.challenge.service.ChallengeService;
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

    /**
     * 결제 초기화 - "결제하기" 버튼 클릭 시 호출
     * PENDING 상태로 UserChallenge, Payment 생성
     */
    @Transactional
    public String initializePayment(PaymentInitRequest request) {
        log.info("결제 초기화 시작 - challengeId: {}, userId: {}", request.getChallengeId(), request.getUserId());
        
        try {
            // 1. 챌린지 참여 가능 여부 검증
            challengeService.validateJoinable(request.getChallengeId(), request.getUserId());
            
            // 2. PENDING 상태로 UserChallenge + Payment 생성
            String uuid = challengeService.createPendingUserChallenge(request.getChallengeId(), request.getUserId());
            
            log.info("결제 초기화 완료 - uuid: {}", uuid);
            return uuid;
            
        } catch (Exception e) {
            log.error("결제 초기화 실패", e);
            throw new RuntimeException("결제 초기화에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 결제 완료 처리 - 외부 결제 API 콜백에서 호출
     * 결제 검증 후 상태를 COMPLETE로 변경
     */
    @Transactional
    public IamportResponse<Payment> processPaymentCallback(PaymentCallbackRequest request) {
        log.info("결제 콜백 처리 시작 - impUid: {}, uuid: {}", request.getPaymentUid(), request.getUuid());
        
        try {
            // 1. 외부 결제 검증
            IamportResponse<Payment> iamportResponse = paymentService.verifyExternalPayment(request.getPaymentUid());
            
            // 2. DB의 결제 정보와 대조 검증
            paymentService.validatePaymentAmount(request.getUuid(), iamportResponse.getResponse());
            
            // 3. 상태 변경: PENDING → COMPLETE
            challengeService.completeUserChallenge(request.getUuid());
            paymentService.completePayment(request.getUuid(), request.getPaymentUid());
            
            log.info("결제 완료 처리 성공 - uuid: {}", request.getUuid());
            return iamportResponse;
            
        } catch (Exception e) {
            log.error("결제 완료 처리 실패", e);
            
            // 실패 시 정리 작업
            cleanupFailedPayment(request.getUuid(), request.getPaymentUid());
            throw new RuntimeException("결제 처리에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 실패한 결제 정리
     */
    private void cleanupFailedPayment(String uuid, String paymentUid) {
        try {
            // 1. DB에서 PENDING -> FAIL
            challengeService.updateFailUserChallenge(uuid);
            //paymentService.updateFailPayment(paymentUid);
            
            // 2. 외부 결제 취소
            if (paymentUid != null && !paymentUid.isEmpty()) {
                paymentService.cancelExternalPayment(paymentUid);
            }
            
            log.info("실패한 결제 정리 완료 - uuid: {}", uuid);
        } catch (Exception e) {
            log.error("결제 실패 처리 중 오류 발생", e);
        }
    }

    // TODO: 결제 취소(환불) 처리
}
