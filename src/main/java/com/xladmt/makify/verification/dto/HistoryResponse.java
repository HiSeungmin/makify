package com.xladmt.makify.verification.dto;

import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.ChallengeRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record HistoryResponse(
        Long id,
        String imageUrl,
        String memo,
        String verificatedDate,
        boolean approved,       // isApproved == Y
        String refusalReason
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static HistoryResponse from(ChallengeRecord record) {
        return new HistoryResponse(
                record.getId(),
                record.getImageUrl(),
                record.getMemo(),
                record.getVerificatedDate().format(FORMATTER),
                YN.Y.equals(record.getIsApproved()),
                record.getRefusalReason()
        );
    }
}
