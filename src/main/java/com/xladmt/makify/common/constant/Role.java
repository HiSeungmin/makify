package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("관리자"),
    USER("회원");

    private final String description;
}