package com.xladmt.makify.member.controller;

import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.common.validator.SignUpValidator;
import com.xladmt.makify.member.dto.SignupRequest;
import com.xladmt.makify.member.dto.SignupResponse;
import com.xladmt.makify.member.service.MemberService;
import lombok.RequiredArgsConstructor;
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
        return "member/login";
    }


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
        model.addAttribute("mypageData", memberService.mypage(member.getId()));
        model.addAttribute("challenges", memberService.myChallenge(member.getId()));
        return "member/mypage";
    }


    @GetMapping("/mypage/challenges")
    public String mypageChallenges(@AuthenticationPrincipal MemberDetails member, Model model) {
        model.addAttribute("challenges",memberService.myChallenge(member.getId()));
        return "member/mypage";
    }

    @GetMapping("/mypage/reviews")
    public String mypageReviews(@AuthenticationPrincipal MemberDetails member, Model model) {

        return "member/mypage";
    }

    @GetMapping("/mypage/inquiry")
    public String mypageInquiry(@AuthenticationPrincipal MemberDetails member, Model model) {
        return "member/mypage";
    }

}
