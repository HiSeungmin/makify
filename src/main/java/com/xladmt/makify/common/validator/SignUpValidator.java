package com.xladmt.makify.common.validator;

import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.dto.SignupRequest;
import com.xladmt.makify.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SignUpValidator {
    private final MemberRepository memberRepository;

    // 이메일 정규식 - RFC 5322 단순 버전
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // 전화번호 정규식 - 010-1234-5678, 02-123-4567, 031-1234-5678 등 지원
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^0\\d{1,2}-?\\d{3,4}-?\\d{4}$");

    /**
     * 회원가입 입력값 검증
     */
    public void validateSignupRequest(SignupRequest request) {
        // 아이디 검증
        if (request.getLoginId() == null || request.getLoginId().isBlank()) {
            throw new BusinessException(ErrorCode.LOGIN_ID_REQUIRED);
        }
        
        if (request.getLoginId().length() < 3 || request.getLoginId().length() > 20) {
            throw new BusinessException(ErrorCode.LOGIN_ID_INVALID_LENGTH);
        }
        
        if (memberRepository.findByLoginId(request.getLoginId()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        // 비밀번호 검증
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException(ErrorCode.PASSWORD_REQUIRED);
        }
        
        if (request.getPassword().length() < 6) {
            throw new BusinessException(ErrorCode.PASSWORD_INVALID_LENGTH);
        }

        // 이름 검증
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException(ErrorCode.NAME_REQUIRED);
        }

        // 닉네임 검증
        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new BusinessException(ErrorCode.NICKNAME_REQUIRED);
        }

        // 이메일 검증
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException(ErrorCode.EMAIL_REQUIRED);
        }
        
        validateEmail(request.getEmail());

        // 전화번호 검증
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new BusinessException(ErrorCode.PHONE_REQUIRED);
        }
        
        validatePhoneNumber(request.getPhone());
    }

    /**
     * 이메일 유효성 검증
     * 형식: user@domain.xxx
     * - @ 필수
     * - @ 이전에 최소 1글자 이상
     * - @ 이후에 도메인명.확장자 형식 (점이 필수는 아님, gmail.xxx 같은 형식)
     */
    private void validateEmail(String email) {
        // 기본 형식 검증
        if (!email.contains("@")) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        }

        String[] parts = email.split("@");
        if (parts.length != 2) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        }

        // 로컬 부분 검증 (@ 앞)
        if (parts[0].isBlank() || parts[0].length() > 64) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        }

        // 도메인 부분 검증 (@ 뒤)
        if (parts[1].isBlank() || parts[1].length() > 255) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        }

        // 정규식 검증
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        }
    }

    /**
     * 전화번호 유효성 검증
     * 지원 형식:
     * - 휴대폰: 010-1234-5678, 010 1234 5678, 01012345678
     * - 지역번호: 02-123-4567, 031-1234-5678 등
     * - 형식: 0X(X)-XXX(X)-XXXX (하이픈 또는 공백 무관)
     */
    private void validatePhoneNumber(String phone) {
        // 공백 제거
        String cleanPhone = phone.replaceAll("\\s+", "-");
        
        // 정규식 검증
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            throw new BusinessException(ErrorCode.PHONE_INVALID_FORMAT);
        }
    }
}
