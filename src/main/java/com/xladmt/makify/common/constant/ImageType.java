package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {
    PROFILE("회원 프로필"),
    VERIFICATION("인증 이미지"),
    VERIFICATION_EXAMPLE("인증 예시 이미지"),
    CHALLENGE("챌린지 대표 이미지"),
    EXPLANATION("챌린지 설명");

    private final String description;
}
