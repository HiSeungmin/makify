package com.xladmt.makify.common.validator;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Component
public class ChallengeCreateRequestValidator implements Validator {

    private static final Map<String, ErrorCode> FIELD_ERROR_CODE_MAP = Map.of(
            "title", ErrorCode.CHALLENGE_TITLE_REQUIRED,
            "description", ErrorCode.CHALLENGE_DESCRIPTION_REQUIRED,
            "category", ErrorCode.CHALLENGE_CATEGORY_REQUIRED,
            "endDate", ErrorCode.CHALLENGE_INVALID_DATE_RANGE,
            "endTime", ErrorCode.CHALLENGE_INVALID_TIME_RANGE,
            "privateCode", ErrorCode.CHALLENGE_PRIVATE_CODE_REQUIRED,
            "fixedDeposit", ErrorCode.CHALLENGE_FIXED_DEPOSIT_REQUIRED,
            "maxDeposit", ErrorCode.CHALLENGE_MAX_DEPOSIT_REQUIRED,
            "minDailyCount", ErrorCode.CHALLENGE_MIN_DAILY_COUNT_REQUIRED
    );

    @Override
    public boolean supports(Class<?> clazz) {
        return ChallengeCreateRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ChallengeCreateRequest request = (ChallengeCreateRequest) target;

        // 날짜 검증
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                errors.rejectValue("endDate", "InvalidDate", "종료 날짜는 시작 날짜보다 빠를 수 없습니다.");
            }
        }

        // 시간 검증
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                errors.rejectValue("endTime", "InvalidTime", "인증 종료 시간은 시작 시간보다 빠를 수 없습니다.");
            }
        }

        // 공개 여부 - 비공개일 경우 privateCode 필요
        if (request.getIsPublic() == YN.N && (request.getPrivateCode() == null || request.getPrivateCode().isBlank())) {
            errors.rejectValue("privateCode", "Required", "비공개 챌린지는 참여 코드가 필요합니다.");
        }

        // 예치금 조건
        if (request.getIsFixedDeposit() == YN.Y) {
            if (request.getFixedDeposit() == null) {
                errors.rejectValue("fixedDeposit", "Required", "고정 예치금이 필요합니다.");
            } else {
                BigDecimal fixedDeposit = request.getFixedDeposit();
                BigDecimal min = BigDecimal.valueOf(1000);
                BigDecimal max = BigDecimal.valueOf(200000);

                if (fixedDeposit.compareTo(min) < 0 || fixedDeposit.compareTo(max) > 0) {
                    errors.rejectValue("fixedDeposit", "InvalidValue",
                            "고정 예치금은 1,000원 이상 200,000원 이하이어야 합니다.");
                }
            }
        } else if (request.getIsFixedDeposit() == YN.N) {
            if (request.getMaxDeposit() == null) {
                errors.rejectValue("maxDeposit", "Required", "최대 예치금이 필요합니다.");
            } else {
                BigDecimal maxDeposit = request.getMaxDeposit();
                BigDecimal min = BigDecimal.valueOf(1000);
                BigDecimal max = BigDecimal.valueOf(200000);

                if (maxDeposit.compareTo(min) < 0 || maxDeposit.compareTo(max) > 0) {
                    errors.rejectValue("maxDeposit", "InvalidValue",
                            "최대 예치금은 1,000원 이상 200,000원 이하이어야 합니다.");
                }
            }
        }

        // 기타 null/blank 검증 (이미 DTO에 @NotBlank 등 붙어 있다면 생략 가능)
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            errors.rejectValue("title", "Required", "제목은 필수입니다.");
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            errors.rejectValue("description", "Required", "설명은 필수입니다.");
        }

        if (request.getCategory() == null) {
            errors.rejectValue("category", "Required", "카테고리를 선택해주세요.");
        }
    }

    public ErrorCode resolveErrorCode(BindingResult bindingResult) {
        FieldError firstError = (FieldError) bindingResult.getAllErrors().get(0);
        String field = firstError.getField();
        return FIELD_ERROR_CODE_MAP.getOrDefault(field, ErrorCode.INVALID_REQUEST);
    }
}