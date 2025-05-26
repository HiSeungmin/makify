package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    ROUTINE("규칙적인 생활"),
    EXCERCISE("운동"),
    MINDSET("마음챙김"),
    EATING_HABITS("식습관"),
    HOBBY("취미"),
    STUDY("공부");

    private final String description;
}
