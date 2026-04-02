package com.xladmt.makify.verification.controller;

import com.xladmt.makify.common.config.security.MemberDetails;
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

        return "verification/verify";
    }

    @GetMapping("/api/challenges/{id}/verify/validate")
    @ResponseBody
    public ResponseEntity<Map<String, String>> validateVerifyTime(@PathVariable Long id) {
        verificationService.validateVerifyTime(id);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/challenges/{id}/verify")
    public String verify(@PathVariable Long id,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam(value = "isPublic", required = false, defaultValue = "false") boolean isPublic,
                         @RequestParam(value = "memo", required = false) String memo,
                         @AuthenticationPrincipal MemberDetails memberDetails) throws IOException {
        verificationService.verify(id, memberDetails.getMember().getId(), image, isPublic, memo);
        return "redirect:/mypage";
    }

    @DeleteMapping("/api/records/{recordId}")
    @ResponseBody
    public ResponseEntity<Void> deleteVerify(@PathVariable Long recordId,
                                             @AuthenticationPrincipal MemberDetails memberDetails) {
        verificationService.deleteVerify(recordId, memberDetails.getMember().getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/records/{recordId}/toggle-public")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> togglePublic(@PathVariable Long recordId,
                                                             @AuthenticationPrincipal MemberDetails memberDetails) {
        boolean isPublic = verificationService.togglePublic(recordId, memberDetails.getMember().getId());
        return ResponseEntity.ok(Map.of("isPublic", isPublic));
    }

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
        model.addAttribute("otherRecords", verificationService.getOtherRecords(id, memberId));
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("todayCount", todayCount);

        return "verification/history";
    }
}
