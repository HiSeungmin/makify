package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.VerificationType;
import com.xladmt.makify.common.constant.YN;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

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
    public Integer fixedDeposit;
    public Integer maxDeposit;

    public Category category;
    private List<MultipartFile> images;

    @Override
    public String toString() {
        return "ChallengeCreateRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", verificationType=" + verificationType +
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
