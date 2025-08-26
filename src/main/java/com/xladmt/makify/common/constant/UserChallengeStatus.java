package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserChallengeStatus {
    PENDING("대기"),
    JOINED("참여"),
    CANCEL("사용자 취소"),
    FAIL("실패"),
    COMPLETED("완료");

    private final String description;
}
