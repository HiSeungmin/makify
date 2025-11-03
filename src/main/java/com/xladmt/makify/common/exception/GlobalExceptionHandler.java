package com.xladmt.makify.common.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리 (JSON 응답)
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ex.printStackTrace();
        ErrorCode errorCode = ex.getErrorCode();

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                errorCode.getMessage(),
                errorCode.getCode()
        );

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * AccessDeniedException (403 Forbidden) 처리
     * - JSON 요청이면 JSON 응답
     * - HTML 요청이면 로그인 페이지로 리다이렉트
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, HttpServletResponse response) {
        ex.printStackTrace();
        
        // 로그인 페이지로 리다이렉트
        try {
            response.sendRedirect("/login");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * 기타 Exception 처리 (JSON 응답)
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception ex) {
        ex.printStackTrace();

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "알 수 없는 오류가 발생했습니다.",
                -1
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    // Error Response DTO
    record ErrorResponse(boolean success, String message, Integer code) {}
}
