package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayoutStatus {
    FAIL("실패"),
    COMPLETE("완료"),
    IN_PROGRESS("진행"),
    PENDING("대기"),
    FAIL("실패");
    private final String description;
}
