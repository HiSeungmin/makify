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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentFacade 테스트")
class PaymentFacadeTest {

    @InjectMocks
    private PaymentFacade paymentFacade;

    @Mock
    private ChallengeService challengeService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ChallengeValidator challengeValidator;

    @Mock
    private PaymentValidator paymentValidator;

    @Nested
    @DisplayName("결제 초기화 테스트")
    class InitializePaymentTest {

        private PaymentInitRequest request;

        @BeforeEach
        void setUp() {
            request = PaymentInitRequest.builder()
                    .challengeId(1L)
                    .userId(1L)
                    .paymentAmount(10000L)
                    .build();
        }

        @Test
        @DisplayName("정상적인 결제 초기화")
        void initializePayment_Success() {
            // given
            Long challengeId = 1L;
            Long userId = 1L;
            String expectedUuid = "test-uuid-123";

            willDoNothing().given(challengeValidator)
                    .validateJoinable(challengeId, userId);
            given(challengeService.createPendingUserChallenge(challengeId, userId))
                    .willReturn(expectedUuid);

            // when
            String result = paymentFacade.initializePayment(request);

            // then
            assertThat(result).isEqualTo(expectedUuid);
            verify(challengeValidator).validateJoinable(challengeId, userId);
            verify(challengeService).createPendingUserChallenge(challengeId, userId);
        }

        @Test
        @DisplayName("챌린지 참여 불가능할 때 예외 발생")
        void initializePayment_ChallengeNotJoinable() {
            // given
            Long challengeId = 1L;
            Long userId = 1L;

            willThrow(new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND))
                    .given(challengeValidator)
                    .validateJoinable(challengeId, userId);

            // when & then
            assertThatThrownBy(() -> paymentFacade.initializePayment(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHALLENGE_NOT_FOUND);

            verify(challengeValidator).validateJoinable(challengeId, userId);
            verifyNoInteractions(challengeService);
        }
    }

    @Nested
    @DisplayName("결제 완료 처리 테스트")
    class ProcessPaymentCallbackTest {

        private PaymentCallbackRequest request;
        private IamportResponse<Payment> mockIamportResponse;
        private Payment mockPayment;

        @BeforeEach
        void setUp() {
            request = new PaymentCallbackRequest();
            // request의 필드 설정 (실제 필드명에 맞게 수정 필요)
            // request.setUuid("test-uuid");
            // request.setPaymentUid("payment-uid-123");

            mockPayment = new Payment();
            mockIamportResponse = new IamportResponse<>();
            //mockIamportResponse.setResponse(mockPayment);
        }

        @Test
        @DisplayName("정상적인 결제 완료 처리")
        void processPaymentCallback_Success() {
            // given
            String uuid = "test-uuid";
            String paymentUid = "payment-uid-123";

            given(paymentService.verifyExternalPayment(paymentUid))
                    .willReturn(mockIamportResponse);
            willDoNothing().given(paymentValidator)
                    .validatePaymentAmount(uuid, mockPayment);
            willDoNothing().given(challengeService).completeUserChallenge(uuid);
            willDoNothing().given(paymentService).completePayment(uuid, paymentUid);

            // when
            IamportResponse<Payment> result = paymentFacade.processPaymentCallback(request);

            // then
            assertThat(result).isEqualTo(mockIamportResponse);
            verify(paymentService).verifyExternalPayment(paymentUid);
            verify(paymentValidator).validatePaymentAmount(uuid, mockPayment);
            verify(challengeService).completeUserChallenge(uuid);
            verify(paymentService).completePayment(uuid, paymentUid);
        }

        @Test
        @DisplayName("외부 결제 검증 실패시 예외 발생 및 정리 작업")
        void processPaymentCallback_ExternalVerificationFail() {
            // given
            String uuid = "test-uuid";
            String paymentUid = "payment-uid-123";

            given(paymentService.verifyExternalPayment(paymentUid))
                    .willThrow(new RuntimeException("외부 결제 검증 실패"));
            
            willDoNothing().given(challengeService).failUserChallenge(uuid);
            willDoNothing().given(paymentService).failPayment(uuid, paymentUid);
            willDoNothing().given(paymentService).cancelExternalPayment(paymentUid);

            // when & then
            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_PROCESSING_FAIL);

            // cleanup 메서드 호출 확인
            verify(challengeService).failUserChallenge(uuid);
            verify(paymentService).failPayment(uuid, paymentUid);
            verify(paymentService).cancelExternalPayment(paymentUid);
        }

        @Test
        @DisplayName("결제 금액 검증 실패시 예외 발생 및 정리 작업")
        void processPaymentCallback_PaymentAmountValidationFail() {
            // given
            String uuid = "test-uuid";
            String paymentUid = "payment-uid-123";

            given(paymentService.verifyExternalPayment(paymentUid))
                    .willReturn(mockIamportResponse);
            willThrow(new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH))
                    .given(paymentValidator)
                    .validatePaymentAmount(uuid, mockPayment);
            
            willDoNothing().given(challengeService).failUserChallenge(uuid);
            willDoNothing().given(paymentService).failPayment(uuid, paymentUid);
            willDoNothing().given(paymentService).cancelExternalPayment(paymentUid);

            // when & then
            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_PROCESSING_FAIL);

            // cleanup 메서드 호출 확인
            verify(challengeService).failUserChallenge(uuid);
            verify(paymentService).failPayment(uuid, paymentUid);
            verify(paymentService).cancelExternalPayment(paymentUid);
        }

        @Test
        @DisplayName("상태 변경 중 실패시 정리 작업")
        void processPaymentCallback_StateChangeFail() {
            // given
            String uuid = "test-uuid";
            String paymentUid = "payment-uid-123";

            given(paymentService.verifyExternalPayment(paymentUid))
                    .willReturn(mockIamportResponse);
            willDoNothing().given(paymentValidator)
                    .validatePaymentAmount(uuid, mockPayment);
            willThrow(new RuntimeException("상태 변경 실패"))
                    .given(challengeService).completeUserChallenge(uuid);
            
            willDoNothing().given(challengeService).failUserChallenge(uuid);
            willDoNothing().given(paymentService).failPayment(uuid, paymentUid);
            willDoNothing().given(paymentService).cancelExternalPayment(paymentUid);

            // when & then
            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_PROCESSING_FAIL);

            // cleanup 메서드 호출 확인
            verify(challengeService).failUserChallenge(uuid);
            verify(paymentService).failPayment(uuid, paymentUid);
            verify(paymentService).cancelExternalPayment(paymentUid);
        }

        @Test
        @DisplayName("paymentUid가 null인 경우 외부 결제 취소하지 않음")
        void processPaymentCallback_NullPaymentUid() {
            // given
            String uuid = "test-uuid";
            String paymentUid = null;
            // request에 null paymentUid 설정

            given(paymentService.verifyExternalPayment(paymentUid))
                    .willThrow(new RuntimeException("결제 처리 실패"));
            
            willDoNothing().given(challengeService).failUserChallenge(uuid);
            willDoNothing().given(paymentService).failPayment(uuid, paymentUid);

            // when & then
            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
                    .isInstanceOf(BusinessException.class);

            // null paymentUid일 때는 외부 결제 취소가 호출되지 않아야 함
            verify(challengeService).failUserChallenge(uuid);
            verify(paymentService).failPayment(uuid, paymentUid);
            verify(paymentService, times(0)).cancelExternalPayment(anyString());
        }

        @Test
        @DisplayName("paymentUid가 빈 문자열인 경우 외부 결제 취소하지 않음")
        void processPaymentCallback_EmptyPaymentUid() {
            // given
            String uuid = "test-uuid";
            String paymentUid = "";
            // request에 빈 문자열 paymentUid 설정

            given(paymentService.verifyExternalPayment(paymentUid))
                    .willThrow(new RuntimeException("결제 처리 실패"));
            
            willDoNothing().given(challengeService).failUserChallenge(uuid);
            willDoNothing().given(paymentService).failPayment(uuid, paymentUid);

            // when & then
            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
                    .isInstanceOf(BusinessException.class);

            // 빈 문자열 paymentUid일 때는 외부 결제 취소가 호출되지 않아야 함
            verify(challengeService).failUserChallenge(uuid);
            verify(paymentService).failPayment(uuid, paymentUid);
            verify(paymentService, times(0)).cancelExternalPayment(anyString());
        }
    }
}
