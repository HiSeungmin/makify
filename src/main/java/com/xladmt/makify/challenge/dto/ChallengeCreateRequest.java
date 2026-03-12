package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.VerificationType;
import com.xladmt.makify.common.constant.YN;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class ChallengeCreateRequest {
    public String title;
    public String description;
    public LocalDate startDate;
    public LocalDate endDate;
    private Integer maxParticipants;

    public VerificationType verificationType;
    public LocalTime startTime;
    public LocalTime endTime;
    public String frequency;
    public Integer minDailyCount;

    public YN isPublic;
    public String privateCode;

    public YN isFixedDeposit;
    public BigDecimal fixedDeposit;
    public BigDecimal maxDeposit;

    public Category category;
    private MultipartFile thumbnailImage;           // 대표 이미지 1장
    private List<MultipartFile> exampleImages;      // 인증 예시 이미지 최대 3장
}
