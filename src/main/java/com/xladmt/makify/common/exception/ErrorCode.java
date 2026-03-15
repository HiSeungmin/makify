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
    DUPLICATE_LOGIN_ID(1003, HttpStatus.BAD_REQUEST, "이미 존재하는 아이디입니다."),
    LOGIN_ID_REQUIRED(1004, HttpStatus.BAD_REQUEST, "아이디를 입력해주세요."),
    LOGIN_ID_INVALID_LENGTH(1005, HttpStatus.BAD_REQUEST, "아이디는 3자 이상 20자 이하여야 합니다."),
    PASSWORD_REQUIRED(1006, HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요."),
    PASSWORD_INVALID_LENGTH(1007, HttpStatus.BAD_REQUEST, "비밀번호는 6자 이상이어야 합니다."),
    NAME_REQUIRED(1008, HttpStatus.BAD_REQUEST, "이름을 입력해주세요."),
    NICKNAME_REQUIRED(1009, HttpStatus.BAD_REQUEST, "닉네임을 입력해주세요."),
    EMAIL_REQUIRED(1010, HttpStatus.BAD_REQUEST, "이메일을 입력해주세요."),
    EMAIL_INVALID_FORMAT(1011, HttpStatus.BAD_REQUEST, "유효한 이메일을 입력해주세요."),
    PHONE_REQUIRED(1012, HttpStatus.BAD_REQUEST, "전화번호를 입력해주세요."),
    PHONE_INVALID_FORMAT(1013, HttpStatus.BAD_REQUEST, "유효한 전화번호를 입력해주세요. (예: 010-1234-5678, 02-123-4567)"),

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

    CHALLENGE_NOT_FOUND(2013,HttpStatus.NOT_FOUND, "챌린지 정보를 찾을 수 없습니다."),
    USER_CHALLENGE_NOT_FOUND(2014,HttpStatus.NOT_FOUND, "사용자가 참여한 챌린지 정보를 찾을 수 없습니다."),
    ALREADY_JOINED_CHALLENGE(2015, HttpStatus.BAD_REQUEST, "이미 참여한 챌린지입니다."),
    CHALLENGE_FULL(2016, HttpStatus.BAD_REQUEST, "참여 가능한 인원이 모두 찬 챌린지입니다."),
    CHALLENGE_ALREADY_STARTED(2017, HttpStatus.BAD_REQUEST, "이미 시작된 챌린지입니다."),

    INVALID_PRIVATE_CODE(2018, HttpStatus.BAD_REQUEST, "참여 코드가 올바르지 않습니다."), // 비공개 챌린지 에러

    // 인증 관련 에러
    VERIFICATION_METHOD_NOT_FOUND(3001, HttpStatus.NOT_FOUND, "인증 수단 정보를 찾을 수 없습니다."),
    VERIFICATION_NOT_ALLOWED_DAY(3002, HttpStatus.BAD_REQUEST, "오늘은 인증 가능한 요일이 아닙니다."),
    VERIFICATION_NOT_ALLOWED_TIME(3003, HttpStatus.BAD_REQUEST, "인증 가능한 시간이 아닙니다."),
    VERIFICATION_NOT_FOUND(3004, HttpStatus.BAD_REQUEST, "인증 내역이 존재하지 않습니다."),

    // 결제 관련 에러
    PAYMENT_INIT_FAIL(4001, HttpStatus.BAD_REQUEST,"결제 초기화에 실패했습니다"),
    PAYMENT_PROCESSING_FAIL(4002, HttpStatus.BAD_REQUEST,"결제 완료 처리에 실패했습니다"),
    PAYMENT_NOT_FAIL(4003, HttpStatus.BAD_REQUEST,"결제 실패 처리에 실패했습니다"),

    PAYMENT_NOT_FOUND(4004, HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_NOT_COMPLETED(4005, HttpStatus.BAD_REQUEST, "외부 결제가 완료되지 않았습니다."),
    PAYMENT_AMOUNT_MISMATCH(4006, HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
    PAYMENT_ALREADY_PROCESSED(4007, HttpStatus.BAD_REQUEST, "이미 처리된 결제입니다."),

    IAMPORT_API_ERROR(4008, HttpStatus.BAD_GATEWAY, "아임포트 API 호출 중 오류가 발생했습니다."),
    IAMPORT_RESPONSE_ERROR(4009, HttpStatus.BAD_GATEWAY, "아임포트 응답 처리 중 오류가 발생했습니다."),


    // 이미지 관련 에러
    FILE_UPLOAD_FAIL(5001, HttpStatus.BAD_REQUEST, "이미지 파일 업로드 중 오류가 발생했습니다."),
    FILE_META_DB_UPLOAD_FAIL(5002, HttpStatus.BAD_REQUEST, "데이터베이스 이미지 파일 업로드 중 오류가 발생했습니다.")


    ;

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
