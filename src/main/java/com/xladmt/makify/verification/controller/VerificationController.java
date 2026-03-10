package com.xladmt.makify.verification.controller;

import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.verification.dto.VerifyResponse;
import com.xladmt.makify.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;


@Controller
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @GetMapping("/challenges/{id}/verify")
    public String showVerifyPage(@PathVariable Long id,
                                 @AuthenticationPrincipal MemberDetails memberDetails,
                                 Model model) {
        VerifyResponse verifyResponse = verificationService.getVerifyPage(id, memberDetails.getMember().getId());

        model.addAttribute("challenge", verifyResponse.challenge());
        model.addAttribute("verificationMethod", verifyResponse.verificationMethod());
        model.addAttribute("targetFrequency", verifyResponse.targetFrequency());
        model.addAttribute("todayVerifiedCount", verifyResponse.todayVerifiedCount());

        return "challenge/verify";
    }

    // 인증 시간 검증 API (페이지 이동 없이 토스트용)
    @GetMapping("/api/challenges/{id}/verify/validate")
    @ResponseBody
    public ResponseEntity<Map<String, String>> validateVerifyTime(@PathVariable Long id) {
        try {
            verificationService.validateVerifyTime(id);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
