package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.ImageType;
import com.xladmt.makify.common.constant.YN;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "FileMeta")
public class FileMeta extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long referenceId; // 참조 ID

    @Enumerated(EnumType.STRING)
    private YN isDeleted;

    @Enumerated(EnumType.STRING)
    private ImageType type;

    private String fileName; // 파일명
    private String path; // 파일 경로
    private String size; // 파일 크기
    private String format; // 확장자
}
