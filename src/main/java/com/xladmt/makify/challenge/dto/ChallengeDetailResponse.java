package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.VerificationType;
import com.xladmt.makify.common.constant.YN;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class ChallengeDetailResponse {

    // 기본 정보
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxParticipants;
    private YN isFixedDeposit;
    private Integer maxDeposit;

    // 개설자
    private String creatorLoginId;

    // 인증 방법
    private String frequencyLabel;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer minDailyCount;
    private String verificationType;

    // 기타
    private Category category;
}
