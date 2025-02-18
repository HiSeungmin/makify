package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    ROUTINE("규칙적인 생활"),
    EXCERCISE("운동"),
    MINDSET("마음챙김"),
    STUDY("공부");

    private final String description;
}
