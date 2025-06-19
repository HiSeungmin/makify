package com.xladmt.makify.common.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, Model model, HttpServletResponse response) {
        ErrorCode errorCode = ex.getErrorCode();
        response.setStatus(errorCode.getHttpStatus().value());

        model.addAttribute("status", errorCode.getHttpStatus().value());
        model.addAttribute("code", errorCode.getCode());
        model.addAttribute("message", errorCode.getMessage());

        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleOtherExceptions(Exception ex, Model model, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        model.addAttribute("status", 500);
        model.addAttribute("code", -1);
        model.addAttribute("message", "알 수 없는 오류가 발생했습니다.");

        return "error/error";
    }
}
