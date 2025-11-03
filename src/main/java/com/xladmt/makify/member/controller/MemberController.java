package com.xladmt.makify.member.controller;

import com.xladmt.makify.common.config.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberController {

    @GetMapping("/login")
    public String loginForm() {
        return "member/login"; // 로그인 폼
    }


    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal MemberDetails member, Model model) {
        model.addAttribute("member", member);
        return "member/mypage";
    }
}
