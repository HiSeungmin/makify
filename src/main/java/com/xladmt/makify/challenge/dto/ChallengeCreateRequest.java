package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.Verifination_Method;
import com.xladmt.makify.common.constant.YN;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ChallengeCreateRequest {
    public String title;
    public String description;
    public LocalDate startDate;
    public LocalDate endDate;

    public Verifination_Method verificationMethod;
    public LocalTime startTime;
    public LocalTime endTime;
    public String frequency;
    public Integer minDailyCount;

    public YN isPublic;
    public String privateCode;

    public YN isFixedDeposit;
    public Integer fixedDeposit;
    public Integer maxDeposit;

    public Category category;

    @Override
    public String toString() {
        return "ChallengeCreateRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", verificationMethod=" + verificationMethod +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", frequency='" + frequency + '\'' +
                ", minDailyCount=" + minDailyCount +
                ", isPublic=" + isPublic +
                ", privateCode='" + privateCode + '\'' +
                ", isFixedDeposit=" + isFixedDeposit +
                ", fixedDeposit=" + fixedDeposit +
                ", maxDeposit=" + maxDeposit +
                ", category=" + category +
                '}';
    }
}
