package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.Verifination_Method;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "VerificationMethod")
public class VerificationMethod extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "explanation", nullable = false, length = 100)
    private String explanation; // 인증 방법 설명

    @Column(name = "count", nullable = false)
    private Integer count; // 인증 빈도

    @Column(name = "start_time", nullable = false)
    private String startTime; // 인증 시작 시간

    @Column(name = "end_time", nullable = false)
    private String endTime; // 인증 종료 시간

    private Integer countInDay; // 하루 인증 횟수
    private Integer countAll; // 전체 인증 횟수

    @Enumerated(EnumType.STRING)
    private Verifination_Method method; // 인증 수단
}
