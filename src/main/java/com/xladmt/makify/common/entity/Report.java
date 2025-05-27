package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.ReportStatus;
import com.xladmt.makify.common.constant.ReportType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Report")
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member; // 신고자

    @Enumerated(EnumType.STRING)
    private ReportType type; // 인증 기록

    private Long referenceId; // 신고 참조 ID (인증샷, 게시글 ID)

    private String reason; // 신고 사유

    @Enumerated(EnumType.STRING)
    private ReportStatus status; // 신고 상태

    private String reportedDate; // 신고 날짜
}
