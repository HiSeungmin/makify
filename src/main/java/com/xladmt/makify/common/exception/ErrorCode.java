package com.xladmt.makify.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 회원 관련 에러
    MEMBER_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
    INVALID_REQUEST(1002, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // 챌린지 관련 에러
    CHALLENGE_TITLE_REQUIRED(2001, HttpStatus.BAD_REQUEST, "챌린지 제목은 필수입니다."),
    CHALLENGE_DESCRIPTION_REQUIRED(2002, HttpStatus.BAD_REQUEST, "챌린지 설명은 필수입니다."),
    CHALLENGE_CATEGORY_REQUIRED(2003, HttpStatus.BAD_REQUEST, "챌린지 카테고리를 선택해주세요."),
    CHALLENGE_INVALID_DATE_RANGE(2004, HttpStatus.BAD_REQUEST, "종료 날짜는 시작 날짜보다 빠를 수 없습니다."),
    CHALLENGE_INVALID_TIME_RANGE(2005, HttpStatus.BAD_REQUEST, "인증 종료 시간은 시작 시간보다 빠를 수 없습니다."),
    CHALLENGE_PRIVATE_CODE_REQUIRED(2006, HttpStatus.BAD_REQUEST, "비공개 챌린지는 참여 코드가 필요합니다."),
    CHALLENGE_FIXED_DEPOSIT_REQUIRED(2007, HttpStatus.BAD_REQUEST, "고정 예치금이 필요합니다."),
    CHALLENGE_MAX_DEPOSIT_REQUIRED(2008, HttpStatus.BAD_REQUEST, "최대 예치금이 필요합니다."),
    CHALLENGE_FIXED_DEPOSIT_INVALID_RANGE(2009, HttpStatus.BAD_REQUEST, "고정 예치금은 1,000원 이상 200,000원 이하여야 합니다."),
    CHALLENGE_MAX_DEPOSIT_INVALID_RANGE(2010, HttpStatus.BAD_REQUEST, "최대 예치금은 1,000원 이상 200,000원 이하여야 합니다."),
    CHALLENGE_MIN_DAILY_COUNT_REQUIRED(2011, HttpStatus.BAD_REQUEST, "최소 인증 횟수는 필수입니다."),
    CHALLENGE_MIN_DAILY_COUNT_INVALID(2012, HttpStatus.BAD_REQUEST, "최소 인증 횟수는 1 이상이어야 합니다."),

    CHALLENGE_NOT_FOUND(2013,HttpStatus.NOT_FOUND, "챌린지 정보를 찾을 수 없습니다.")


    ;

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
