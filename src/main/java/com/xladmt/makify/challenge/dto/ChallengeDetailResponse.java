package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.ChallengeStatus;
import com.xladmt.makify.common.constant.VerificationType;
import com.xladmt.makify.common.constant.YN;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
    private BigDecimal maxDeposit;
    private ChallengeStatus status;
    private Integer progressPercentage;

    // 개설자
    private String creatorLoginId;
    private String creatorNickName;

    // 인증 방법
    private String frequencyLabel;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer minDailyCount;
    private String verificationType;

    // 이미지
    private String thumbnailUrl;           // 대표 이미지
    private List<String> exampleImageUrls; // 인증 예시 이미지 (S3 퍼블릭 URL)

    // 기타
    private String category;
    private boolean alreadyJoined;
    private Long currentParticipants;
}
