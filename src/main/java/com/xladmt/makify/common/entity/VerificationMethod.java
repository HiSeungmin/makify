package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.Frequency;
import com.xladmt.makify.common.constant.VerificationType;
import com.xladmt.makify.common.constant.YN;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "VerificationMethod")
public class VerificationMethod extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Frequency frequency; // 주당 인증 빈도

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // 인증 시작 시간

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime; // 인증 종료 시간

    private Integer minDailyCount; // 하루 인증 횟수

    @Enumerated(EnumType.STRING)
    private VerificationType method; // 인증 수단

    @Enumerated(EnumType.STRING)
    private YN isVisible;

    @Enumerated(EnumType.STRING)
    @Column(name = "has_example_image")
    private YN hasExampleImage = YN.N; // 인증 예시 이미지 존재 여부

    public static VerificationMethod create(Frequency frequency,
                                             LocalTime startTime,
                                             LocalTime endTime,
                                             Integer minDailyCount,
                                             VerificationType method,
                                            YN hasExampleImage) {
        VerificationMethod setting = new VerificationMethod();
        setting.frequency = frequency;
        setting.startTime = startTime;
        setting.endTime = endTime;
        setting.minDailyCount = minDailyCount;
        setting.method = method;
        setting.isVisible = YN.Y;
        setting.hasExampleImage = hasExampleImage;
        return setting;
    }

    public void update(Frequency frequency,
                       LocalTime startTime,
                       LocalTime endTime,
                       Integer minDailyCount,
                       VerificationType method) {
        this.frequency = frequency;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minDailyCount = minDailyCount;
        this.method = method;
    }

    public void markHasExampleImage() {
        this.hasExampleImage = YN.Y;
    }

    public void delete() {
        this.isVisible = YN.N;
    }
}
