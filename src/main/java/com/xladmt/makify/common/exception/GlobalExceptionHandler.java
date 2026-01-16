package com.xladmt.makify.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException ex, HttpServletRequest request) {
        ex.printStackTrace();
        ErrorCode errorCode = ex.getErrorCode();

        // HTML 요청인 경우
        if (isHtmlRequest(request)) {
            ModelAndView mav = new ModelAndView("error/error");
            mav.addObject("message", errorCode.getMessage());
            mav.addObject("status", errorCode.getHttpStatus().value());
            return mav;
        }

        // JSON 요청인 경우
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
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request, HttpServletResponse response) {
        ex.printStackTrace();

        // HTML 요청인 경우 로그인 페이지로 리다이렉트
        if (isHtmlRequest(request)) {
            try {
                response.sendRedirect("/login");
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        // JSON 요청인 경우
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(false, "접근 권한이 없습니다.", 403));
    }

    /**
     * 기타 Exception 처리
     */
    @ExceptionHandler(Exception.class)
    public Object handleOtherExceptions(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();

        // HTML 요청인 경우
        if (isHtmlRequest(request)) {
            ModelAndView mav = new ModelAndView("error/error");
            mav.addObject("message", "알 수 없는 오류가 발생했습니다.");
            mav.addObject("status", 500);
            return mav;
        }

        // JSON 요청인 경우
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "알 수 없는 오류가 발생했습니다.",
                -1
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * HTML 요청인지 확인하는 헬퍼 메서드
     */
    private boolean isHtmlRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        return acceptHeader != null && acceptHeader.contains("text/html");
    }

    // Error Response DTO
    record ErrorResponse(boolean success, String message, Integer code) {}
}
