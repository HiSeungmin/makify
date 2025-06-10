package com.xladmt.makify.challenge.dto;

import lombok.Data;

@Data
public class ChallengeDto {
    private Long id;
    private String title;
    private String imageUrl;
    private String startDate;
    private String endDate;
    //private VerificationDto verificationId;
}
