package com.xladmt.makify.common.validator;

import com.xladmt.makify.common.constant.Frequency;
import com.xladmt.makify.verification.domain.VerificationMethod;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class VerifyValidator {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 인증 가능 요일 + 시간대 종합 검증
     */
    public void validate(VerificationMethod method) {
        validateDay(method.getFrequency());
        validateTime(method.getStartTime(), method.getEndTime());
    }

    /**
     * 인증 가능 요일 검증 (Frequency 기반)
     */
    private void validateDay(Frequency frequency) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        boolean isWeekend = (today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY);

        boolean allowed = switch (frequency) {
            case WEEKDAYS -> !isWeekend;
            case WEEKENDS -> isWeekend;
            default -> true; // DAILY, 주 N회, CUSTOM 등은 요일 제한 없음
        };

        if (!allowed) {
            String allowedDays = (frequency == Frequency.WEEKDAYS) ? "월~금" : "토, 일";
            throw new BusinessException(ErrorCode.VERIFICATION_NOT_ALLOWED_DAY,
                    "오늘은 인증 가능한 요일이 아닙니다. 인증 가능 요일: " + allowedDays);
        }
    }

    /**
     * 인증 가능 시간대 검증
     */
    private void validateTime(LocalTime startTime, LocalTime endTime) {
        LocalTime now = LocalTime.now();

        if (now.isBefore(startTime) || now.isAfter(endTime)) {
            String timeRange = startTime.format(TIME_FORMATTER) + " ~ " + endTime.format(TIME_FORMATTER);
            throw new BusinessException(ErrorCode.VERIFICATION_NOT_ALLOWED_TIME,
                    "인증 시간이 아닙니다. (인증 가능 시간: " + timeRange+")");
        }
    }
}
