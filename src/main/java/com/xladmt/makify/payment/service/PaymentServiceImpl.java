package com.xladmt.makify.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.constant.PaidStatus;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserChallengeRepository userChallengeRepository;
    private final ObjectMapper objectMapper;

    @Value("${IAMPORT_SECRET_KEY}")
    private String secretKey;

    private static final String PORTONE_V2_URL = "https://api.portone.io";

    /**
     * 포트원 V2 REST API로 결제 검증
     */
    @Override
    public IamportResponse<Payment> verifyExternalPayment(String paymentUid) {
        if (paymentUid == null || paymentUid.isBlank()) {
            throw new BusinessException(ErrorCode.IAMPORT_RESPONSE_ERROR);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "PortOne " + secretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                    PORTONE_V2_URL + "/payments/" + paymentUid,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());
            String status = json.path("status").asText();
            int amount = json.path("amount").path("total").asInt();

            log.info("포트원 V2 결제 조회 성공: paymentId={}, status={}, amount={}", paymentUid, status, amount);

            if (!"PAID".equals(status)) {
                log.warn("결제 미완료 상태: status={}", status);
                throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
            }

            return new V2PaymentResponse(paymentUid, amount);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("포트원 V2 결제 검증 실패: paymentUid={}", paymentUid, e);
            throw new BusinessException(ErrorCode.IAMPORT_RESPONSE_ERROR);
        }
    }

    @Override
    public void completePayment(String uuid, String paymentUid) {
        UserChallenge userChallenge = userChallengeRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보가 존재하지 않습니다."));
        userChallenge.getPayment().updateStatus(PaidStatus.COMPLETE, paymentUid);
    }

    @Override
    public void failPayment(String uuid, String paymentUid) {
        UserChallenge userChallenge = userChallengeRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보가 존재하지 않습니다."));
        userChallenge.getPayment().updateStatus(PaidStatus.FAIL, paymentUid);
    }

    /**
     * 포트원 V2 REST API로 결제 취소
     */
    @Override
    public void cancelExternalPayment(String paymentUid) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "PortOne " + secretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = "{\"reason\": \"결제 검증 실패로 인한 자동 취소\"}";

            restTemplate.exchange(
                    PORTONE_V2_URL + "/payments/" + paymentUid + "/cancel",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            log.info("포트원 V2 결제 취소 완료: paymentId={}", paymentUid);

        } catch (Exception e) {
            log.error("포트원 V2 결제 취소 실패: paymentUid={}", paymentUid, e);
            throw new BusinessException(ErrorCode.IAMPORT_RESPONSE_ERROR);
        }
    }

    /**
     * 포트원 V2 응답을 기존 IamportResponse<Payment> 형태로 래핑
     */
    private static class V2PaymentResponse extends IamportResponse<Payment> {
        private final Payment payment;

        public V2PaymentResponse(String paymentId, int amount) {
            this.payment = new Payment() {
                @Override
                public String getImpUid() { return paymentId; }

                @Override
                public BigDecimal getAmount() { return BigDecimal.valueOf(amount); }

                @Override
                public String getStatus() { return "paid"; }
            };
        }

        @Override
        public Payment getResponse() { return payment; }
    }
}
