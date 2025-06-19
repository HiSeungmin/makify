package com.xladmt.makify.common.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Frequency {

    DAILY(7, "매일"),
    WEEKDAYS(5, "평일"),
    WEEKENDS(2, "주말"),
    ONE_PER_WEEK(1, "주 1회"),
    TWO_PER_WEEK(2, "주 2회"),
    THREE_PER_WEEK(3, "주 3회"),
    FOUR_PER_WEEK(4, "주 4회"),
    FIVE_PER_WEEK(5, "주 5회"),
    SIX_PER_WEEK(6, "주 6회"),
    CUSTOM(0, "기타(제한 없음)");

    private final int countPerWeek; // 주당 횟수
    private final String label;     // 프론트/관리자용 레이블

    Frequency(int countPerWeek, String label) {
        this.countPerWeek = countPerWeek;
        this.label = label;
    }

    public int getCountPerWeek() {
        return countPerWeek;
    }

    public String getLabel() {
        return label;
    }

    // 매칭용 Map
    private static final Map<String, Frequency> labelMap =
            Arrays.stream(Frequency.values())
                    .collect(Collectors.toMap(Frequency::getLabel, f -> f));

    public static Frequency fromLabel(String label) {
        return labelMap.get(label);
    }
}
