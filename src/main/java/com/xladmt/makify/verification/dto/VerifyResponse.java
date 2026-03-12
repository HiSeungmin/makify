package com.xladmt.makify.verification.dto;

import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.VerificationMethod;

public record VerifyResponse (Challenge challenge,
        VerificationMethod verificationMethod,
        int targetFrequency,
        int todayVerifiedCount){
}
