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
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

        @ParameterizedTest
        @CsvSource({
                "CHALLENGE_NOT_FOUND, 챌린지 정보를 찾을 수 없습니다., validateChallengeExists() 단계 실패",
                "MEMBER_NOT_FOUND, 회원 정보를 찾을 수 없습니다., validateMemberExists() 단계 실패",
                "ALREADY_JOINED_CHALLENGE, 이미 참여한 챌린지입니다., validateNotAlreadyJoined() 단계 실패",
                "CHALLENGE_FULL, 참여 가능한 인원이 모두 찬 챌린지입니다., validateChallengeCapacity() 단계 실패",
                "CHALLENGE_ALREADY_STARTED, 이미 시작된 챌린지입니다., validateChallengeStartDate() 단계 실패"
        })
        @DisplayName("챌린지 참여 불가능한 각종 케이스 - ChallengeValidator 검증 단계별 실패")
        void challengeNotJoinable_VariousValidationFailures(String errorCodeName, String expectedMessage, String scenario) {
            // given
            ErrorCode errorCode = ErrorCode.valueOf(errorCodeName);
            willThrow(new BusinessException(errorCode))
                    .given(challengeValidator).validateJoinable(CHALLENGE_ID, USER_ID);

            // when & then
            assertThatThrownBy(() -> paymentFacade.initializePayment(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", errorCode)
                    .hasMessage(expectedMessage);

            // 검증: challengeValidator.validateJoinable()이 호출되었는지 확인
            verify(challengeValidator).validateJoinable(CHALLENGE_ID, USER_ID);
            // 검증: challengeService는 호출되지 않았는지 확인 (예외 발생으로 인해)
            verifyNoInteractions(challengeService);
        }


    }

//    @Nested
//    @DisplayName("결제 콜백 처리 테스트")
//    class ProcessPaymentCallbackTest {
//
//        private PaymentCallbackRequest request;
//        private IamportResponse<Payment> mockIamportResponse;
//        private Payment mockPayment;
//
//        private static final String UUID = "test-uuid-123";
//        private static final String PAYMENT_UID = "payment-uid-456";
//
//        @BeforeEach
//        void setUp() {
//            request = PaymentCallbackRequest.builder()
//                    .uuid(UUID)
//                    .paymentUid(PAYMENT_UID)
//                    .build();
//
//            mockPayment = new Payment();
//            mockPayment.setAmount(BigDecimal.valueOf(10000));
//            mockPayment.setStatus("paid");
//
//            mockIamportResponse = new IamportResponse<>();
//            mockIamportResponse.setResponse(mockPayment);
//        }
//
//        @Test
//        @DisplayName("정상적인 결제 콜백 처리 - 성공")
//        void processPaymentCallback_Success() {
//            // given
//            given(paymentService.verifyExternalPayment(PAYMENT_UID))
//                    .willReturn(mockIamportResponse);
//            willDoNothing().given(paymentValidator).validatePaymentAmount(UUID, mockPayment);
//            willDoNothing().given(challengeService).completeUserChallenge(UUID);
//            willDoNothing().given(paymentService).completePayment(UUID, PAYMENT_UID);
//
//            // when
//            IamportResponse<Payment> result = paymentFacade.processPaymentCallback(request);
//
//            // then
//            assertThat(result).isEqualTo(mockIamportResponse);
//            verify(paymentService).verifyExternalPayment(PAYMENT_UID);
//            verify(paymentValidator).validatePaymentAmount(UUID, mockPayment);
//            verify(challengeService).completeUserChallenge(UUID);
//            verify(paymentService).completePayment(UUID, PAYMENT_UID);
//        }
//
//        @Test
//        @DisplayName("외부 결제 검증 실패 - 예외 발생 및 정리 작업")
//        void processPaymentCallback_WhenExternalVerificationFails_ThrowsExceptionAndCleanup() {
//            // given
//            given(paymentService.verifyExternalPayment(PAYMENT_UID))
//                    .willThrow(new RuntimeException("External payment verification failed"));
//
//            // when & then
//            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
//                    .isInstanceOf(BusinessException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_PROCESSING_FAIL);
//
//            verify(paymentService).verifyExternalPayment(PAYMENT_UID);
//            // cleanup 메서드 호출 검증
//            verify(challengeService).failUserChallenge(UUID);
//            verify(paymentService).failPayment(UUID, PAYMENT_UID);
//            verify(paymentService).cancelExternalPayment(PAYMENT_UID);
//        }
//
//        @Test
//        @DisplayName("결제 금액 검증 실패 - 예외 발생 및 정리 작업")
//        void processPaymentCallback_WhenAmountValidationFails_ThrowsExceptionAndCleanup() {
//            // given
//            given(paymentService.verifyExternalPayment(PAYMENT_UID))
//                    .willReturn(mockIamportResponse);
//            willThrow(new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH))
//                    .given(paymentValidator).validatePaymentAmount(UUID, mockPayment);
//
//            // when & then
//            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
//                    .isInstanceOf(BusinessException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_PROCESSING_FAIL);
//
//            verify(paymentService).verifyExternalPayment(PAYMENT_UID);
//            verify(paymentValidator).validatePaymentAmount(UUID, mockPayment);
//            // cleanup 메서드 호출 검증
//            verify(challengeService).failUserChallenge(UUID);
//            verify(paymentService).failPayment(UUID, PAYMENT_UID);
//            verify(paymentService).cancelExternalPayment(PAYMENT_UID);
//        }
//
//        @Test
//        @DisplayName("챌린지 완료 처리 실패 - 예외 발생 및 정리 작업")
//        void processPaymentCallback_WhenChallengeCompletionFails_ThrowsExceptionAndCleanup() {
//            // given
//            given(paymentService.verifyExternalPayment(PAYMENT_UID))
//                    .willReturn(mockIamportResponse);
//            willDoNothing().given(paymentValidator).validatePaymentAmount(UUID, mockPayment);
//            willThrow(new RuntimeException("Challenge completion failed"))
//                    .given(challengeService).completeUserChallenge(UUID);
//
//            // when & then
//            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
//                    .isInstanceOf(BusinessException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_PROCESSING_FAIL);
//
//            verify(paymentService).verifyExternalPayment(PAYMENT_UID);
//            verify(paymentValidator).validatePaymentAmount(UUID, mockPayment);
//            verify(challengeService).completeUserChallenge(UUID);
//            // cleanup 메서드 호출 검증
//            verify(challengeService).failUserChallenge(UUID);
//            verify(paymentService).failPayment(UUID, PAYMENT_UID);
//            verify(paymentService).cancelExternalPayment(PAYMENT_UID);
//        }
//
//        @Test
//        @DisplayName("결제 완료 처리 실패 - 예외 발생 및 정리 작업")
//        void processPaymentCallback_WhenPaymentCompletionFails_ThrowsExceptionAndCleanup() {
//            // given
//            given(paymentService.verifyExternalPayment(PAYMENT_UID))
//                    .willReturn(mockIamportResponse);
//            willDoNothing().given(paymentValidator).validatePaymentAmount(UUID, mockPayment);
//            willDoNothing().given(challengeService).completeUserChallenge(UUID);
//            willThrow(new RuntimeException("Payment completion failed"))
//                    .given(paymentService).completePayment(UUID, PAYMENT_UID);
//
//            // when & then
//            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
//                    .isInstanceOf(BusinessException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_PROCESSING_FAIL);
//
//            verify(paymentService).verifyExternalPayment(PAYMENT_UID);
//            verify(paymentValidator).validatePaymentAmount(UUID, mockPayment);
//            verify(challengeService).completeUserChallenge(UUID);
//            verify(paymentService).completePayment(UUID, PAYMENT_UID);
//            // cleanup 메서드 호출 검증
//            verify(challengeService).failUserChallenge(UUID);
//            verify(paymentService).failPayment(UUID, PAYMENT_UID);
//            verify(paymentService).cancelExternalPayment(PAYMENT_UID);
//        }
//
//        @Test
//        @DisplayName("PaymentUid가 null인 경우의 정리 작업 - 외부 결제 취소 미호출")
//        void processPaymentCallback_WhenPaymentUidIsNull_CleanupWithoutExternalCancel() {
//            // given
//            request = PaymentCallbackRequest.builder()
//                    .uuid(UUID)
//                    .paymentUid(null)
//                    .build();
//
//            given(paymentService.verifyExternalPayment(null))
//                    .willThrow(new RuntimeException("Payment verification failed"));
//
//            // when & then
//            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
//                    .isInstanceOf(BusinessException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_PROCESSING_FAIL);
//
//            // cleanup 시 외부 결제 취소는 호출되지 않아야 함
//            verify(challengeService).failUserChallenge(UUID);
//            verify(paymentService).failPayment(UUID, null);
//            verify(paymentService, times(0)).cancelExternalPayment(anyString());
//        }
//
//        @Test
//        @DisplayName("PaymentUid가 빈 문자열인 경우의 정리 작업 - 외부 결제 취소 미호출")
//        void processPaymentCallback_WhenPaymentUidIsEmpty_CleanupWithoutExternalCancel() {
//            // given
//            request = PaymentCallbackRequest.builder()
//                    .uuid(UUID)
//                    .paymentUid("")
//                    .build();
//
//            given(paymentService.verifyExternalPayment(""))
//                    .willThrow(new RuntimeException("Payment verification failed"));
//
//            // when & then
//            assertThatThrownBy(() -> paymentFacade.processPaymentCallback(request))
//                    .isInstanceOf(BusinessException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_PROCESSING_FAIL);
//
//            // cleanup 시 외부 결제 취소는 호출되지 않아야 함
//            verify(challengeService).failUserChallenge(UUID);
//            verify(paymentService).failPayment(UUID, "");
//            verify(paymentService, times(0)).cancelExternalPayment(anyString());
//        }
//    }
}
