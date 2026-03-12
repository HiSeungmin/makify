package com.xladmt.makify.verification.controller;

import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.verification.dto.VerifyResponse;
import com.xladmt.makify.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    // 인증 시간 검증 API
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

    // 인증 제출
    @PostMapping("/challenges/{id}/verify")
    public String verify(@PathVariable Long id,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam(value = "memo", required = false) String memo,
                         @AuthenticationPrincipal MemberDetails memberDetails) throws IOException {
        verificationService.verify(id, memberDetails.getMember().getId(), image, memo);
        return "redirect:/mypage";
    }

    // 인증 내역 페이지
    @GetMapping("/challenges/{id}/history")
    public String showHistoryPage(@PathVariable Long id,
                                  @AuthenticationPrincipal MemberDetails memberDetails,
                                  Model model) {
        Long memberId = memberDetails.getMember().getId();

        VerifyResponse verifyResponse = verificationService.getHistoryPage(id, memberId);
        int totalCount = verificationService.getTotalCount(id, memberId);
        int todayCount = verifyResponse.todayVerifiedCount();

        model.addAttribute("challenge", verifyResponse.challenge());
        model.addAttribute("verificationMethod", verifyResponse.verificationMethod());
        model.addAttribute("records", verificationService.getRecords(id, memberId));
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("todayCount", todayCount);

        return "verification/history";
    }
}
