package com.xladmt.makify.application;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

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

    @Mock
    private IamportClient iamportClient;

    @Nested
    @DisplayName("결제 초기화 테스트")
    class InitializePaymentTest {

        private PaymentInitRequest request;
        private static final Long CHALLENGE_ID = 1L;
        private static final Long USER_ID = 100L;
        private static final String EXPECTED_UUID = "test-uuid-123";

        @BeforeEach
        void setUp() {
            request = PaymentInitRequest.builder()
                    .challengeId(CHALLENGE_ID)
                    .userId(USER_ID)
                    .build();
        }

        @Test
        @DisplayName("정상적인 결제 초기화 - UUID 반환")
        void initializePayment_Success() {
            // given
            willDoNothing().given(challengeValidator).validateJoinable(CHALLENGE_ID, USER_ID);
            given(challengeService.createPendingUserChallenge(CHALLENGE_ID, USER_ID))
                    .willReturn(EXPECTED_UUID);

            // when
            String result = paymentFacade.initializePayment(request);

            // then
            assertThat(result).isEqualTo(EXPECTED_UUID);
            verify(challengeValidator).validateJoinable(CHALLENGE_ID, USER_ID);
            verify(challengeService).createPendingUserChallenge(CHALLENGE_ID, USER_ID);
        }

        @Test
        @DisplayName("결제 초기화 실패 - request가 null인 경우")
        void initializePayment_WhenRequestIsNull() {
            // when & then
            assertThatThrownBy(() -> paymentFacade.initializePayment(null))
                    .isInstanceOf(BusinessException.class);

            // 검증: 다른 서비스들은 호출되지 않았는지 확인
            verifyNoInteractions(challengeValidator);
            verifyNoInteractions(challengeService);
        }

        @Test
        @DisplayName("결제 초기화 실패 - request 필드가 null인 경우")
        void initializePayment_WhenRequestFieldsAreNull_ThrowsException() {
            // given
            PaymentInitRequest invalidRequest = PaymentInitRequest.builder()
                    .challengeId(null)
                    .userId(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> paymentFacade.initializePayment(invalidRequest))
                    .isInstanceOf(BusinessException.class);
        }
    }


    @Nested
    @DisplayName("결제 콜백 처리 테스트")
    class ProcessPaymentCallbackTest {

        private PaymentCallbackRequest request;
        private IamportResponse<Payment> mockIamportResponse;
        @Mock
        private Payment mockPayment;

        private static final String UUID = "test-uuid-123";
        private static final String PAYMENT_UID = "payment-uid-456";

        @BeforeEach
        void setUp() {
            request = PaymentCallbackRequest.builder()
                    .uuid(UUID)
                    .paymentUid(PAYMENT_UID)
                    .build();

            mockPayment = mock(Payment.class);
            mockIamportResponse = mock(IamportResponse.class);
        }

        @Test
        @DisplayName("정상적인 결제 콜백 처리 - 성공")
        void processPaymentCallback_Success() {
            // given

            given(mockIamportResponse.getResponse()).willReturn(mockPayment);
            given(paymentService.verifyExternalPayment(PAYMENT_UID))
                    .willReturn(mockIamportResponse);
            willDoNothing().given(paymentValidator).validatePaymentAmount(UUID, mockPayment);
            willDoNothing().given(challengeService).completeUserChallenge(UUID);
            willDoNothing().given(paymentService).completePayment(UUID, PAYMENT_UID);

            // when
            IamportResponse<Payment> result = paymentFacade.processPaymentCallback(request);

            // then
            assertThat(result).isEqualTo(mockIamportResponse);
            verify(paymentService).verifyExternalPayment(PAYMENT_UID);
            verify(paymentValidator).validatePaymentAmount(UUID, mockPayment);
            verify(challengeService).completeUserChallenge(UUID);
            verify(paymentService).completePayment(UUID, PAYMENT_UID);
        }

        @Test
        @DisplayName("외부 결제 검증 실패 - 결제 상태가 paid가 아님")
        void processPaymentCallback_WhenPaymentNotCompleted() {
            // given
            given(paymentService.verifyExternalPayment(PAYMENT_UID))
                    .willThrow(new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED));

            // when & then
            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_COMPLETED);

            verify(paymentService).verifyExternalPayment(PAYMENT_UID);
            // cleanup 메서드 호출 검증
            verify(challengeService).failUserChallenge(UUID);
            verify(paymentService).failPayment(UUID, PAYMENT_UID);
            verify(paymentService).cancelExternalPayment(PAYMENT_UID);
        }

        @Test
        @DisplayName("외부 결제 검증 실패 - IamportResponseException 또는 IOException")
        void processPaymentCallback_WhenExternalAPIError() {
            // given
            given(paymentService.verifyExternalPayment(PAYMENT_UID))
                    .willThrow(new BusinessException(ErrorCode.IAMPORT_RESPONSE_ERROR));

            // when & then
            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IAMPORT_RESPONSE_ERROR);

            verify(paymentService).verifyExternalPayment(PAYMENT_UID);
            // cleanup 메서드 호출 검증
            verify(challengeService).failUserChallenge(UUID);
            verify(paymentService).failPayment(UUID, PAYMENT_UID);
            verify(paymentService).cancelExternalPayment(PAYMENT_UID);
        }


        @Test
        @DisplayName("DB 결제 정보 검증 실패 - 금액 불일치")
        void processPaymentCallback_WhenAmountMismatch_ThrowsException() {
            // given
            Payment mismatchPayment = mock(Payment.class);

            IamportResponse<Payment> response = mock(IamportResponse.class);
            given(response.getResponse()).willReturn(mismatchPayment);

            given(paymentService.verifyExternalPayment(PAYMENT_UID))
                    .willReturn(response);

            willThrow(new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH))
                    .given(paymentValidator).validatePaymentAmount(UUID, mismatchPayment);

            // when & then
            // PaymentValidator가 실제로 10000 != 15000 비교해서 예외 발생!
            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_AMOUNT_MISMATCH);

            // cleanup 메서드 호출 검증
            verify(challengeService).failUserChallenge(UUID);
            verify(paymentService).failPayment(UUID, PAYMENT_UID);
            verify(paymentService).cancelExternalPayment(PAYMENT_UID);
        }
    }
}
