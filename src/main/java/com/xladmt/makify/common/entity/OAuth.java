package com.xladmt.makify.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "OAuth")
public class OAuth extends BaseEntity {
    @Id
    private String id;

    @OneToOne
    @JoinColumn(name = "Member_id", nullable = false)
    private Member member;
}