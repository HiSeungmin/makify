package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.Verifination_Method;
import com.xladmt.makify.common.constant.YN;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ChallengeCreateRequest {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    private Verifination_Method verificationMethod;
    private LocalTime startTime;
    private LocalTime endTime;
    private String frequency;
    private Integer minCount;

    private YN isPublic;
    private String privateCode;

    private YN isFixedDeposit;
    private Integer fixedDeposit;
    private Integer maxDeposit;

    private Category category;
}
