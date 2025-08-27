package com.xladmt.makify.common.validator;

import com.siot.IamportRestClient.response.Payment;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private final UserChallengeRepository userChallengeRepository;

    public void validatePaymentAmount(String uuid, Payment externalPayment) {
        UserChallenge userChallenge = userChallengeRepository.findByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CHALLENGE_NOT_FOUND));

        Long expectedAmount = userChallenge.getPayment().getAmount();
        Long actualAmount = externalPayment.getAmount().longValue();

        if (!expectedAmount.equals(actualAmount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

}
