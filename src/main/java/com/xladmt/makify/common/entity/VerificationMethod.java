package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.Verifination_Method;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "VerificationMethod")
public class VerificationMethod extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String explanation; // 인증 방법 설명

    private Integer count; // 인증 빈도

    private LocalDateTime startDate; // 인증 시작 시간
    private LocalDateTime endDate; // 인증 종료 시간

    private String countInDay; // 하루 인증 횟수
    private String countAll; // 전체 인증 횟수

    @Enumerated(EnumType.STRING)
    private Verifination_Method verificationMethod; // 인증 수단
}
