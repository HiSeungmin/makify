package com.xladmt.makify.verification.dto;

import com.xladmt.makify.challenge.domain.Challenge;
import com.xladmt.makify.verification.domain.VerificationMethod;

public record VerifyResponse (Challenge challenge,
        VerificationMethod verificationMethod,
        int targetFrequency,
        int todayVerifiedCount){
}
