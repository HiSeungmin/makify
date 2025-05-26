package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengeStatus {
    NOT_STARTED("시작 전"),
    IN_PROGRESS("진행중"),
    COMPLETED("종료"),
    DELETED("삭제");

    private final String description;
}
