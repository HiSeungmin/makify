package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Sledding {
    FAIL("중단"),
    CANCLE("실패"),
    COMPLETE("완료"),
    IN_PROGRESS("진행"),
    PENDING("시작 전");

    private final String description;
}
