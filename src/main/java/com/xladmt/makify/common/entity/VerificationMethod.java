package com.xladmt.makify.common.entity;

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

    private String explanation;
    private String count;
    private String startDate;
    private String endDate;
    private String countInDay;
    private String countAll;
    private String verificationMethod;
}
