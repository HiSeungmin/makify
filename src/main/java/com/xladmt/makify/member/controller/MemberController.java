package com.xladmt.makify.member.controller;

import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.common.validator.SignUpValidator;
import com.xladmt.makify.member.dto.SignupRequest;
import com.xladmt.makify.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final SignUpValidator signUpValidator;

    @GetMapping("/login")
    public String loginForm() {
        return "member/login"; // 로그인 폼
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @ResponseBody
    public SignupResponse signup(@RequestBody SignupRequest request) {
        // 입력값 검증
        signUpValidator.validateSignupRequest(request);

        // 회원가입
        memberService.signup(request);

        return new SignupResponse(true, "회원가입이 완료되었습니다.");
    }



    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal MemberDetails member, Model model) {
        model.addAttribute("member", member);
        return "member/mypage";
    }

    // Response DTO
    record SignupResponse(boolean success, String message) {}
}
