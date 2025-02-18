package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    CHALLENGE("챌린지"),
    POST("게시글");

    private final String description;
}
