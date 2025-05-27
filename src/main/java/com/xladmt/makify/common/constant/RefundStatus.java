package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundStatus {
    FAIL("실패"),
    CANCLE("취소"),
    COMPLETE("완료"),
    IN_PROGRESS("진행"),
    PENDING("대기");

    private final String description;
}
