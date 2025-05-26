package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    RECEIVE("접수"),
    IN_PROGRESS("심사"),
    COMPLETE("완료"),
    CANCEL("취소/반려");
    private final String description;
}
