package com.xladmt.makify.challenge.controller;

import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.service.ReviewService;
import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ChallengeRepository challengeRepository;

    @GetMapping("/challenges/{id}/review")
    public String showReviewPage(@PathVariable Long id,
                                 @AuthenticationPrincipal MemberDetails memberDetails,
                                 Model model) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        Long memberId = memberDetails.getMember().getId();
        boolean alreadyReviewed = reviewService.hasReview(id, memberId);

        model.addAttribute("challenge", challenge);
        model.addAttribute("alreadyReviewed", alreadyReviewed);
        return "challenge/review";
    }

    @PostMapping("/challenges/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @RequestParam("content") String content,
                               @RequestParam("star") Integer star,
                               @AuthenticationPrincipal MemberDetails memberDetails) {
        reviewService.submitReview(id, memberDetails.getMember().getId(), content, star);
        return "redirect:/mypage";
    }

    @PatchMapping("/api/reviews/{reviewId}")
    @ResponseBody
    public ResponseEntity<Void> updateReview(@PathVariable Long reviewId,
                                             @RequestParam("content") String content,
                                             @RequestParam("star") Integer star,
                                             @AuthenticationPrincipal MemberDetails memberDetails) {
        reviewService.updateReview(reviewId, memberDetails.getMember().getId(), content, star);
        return ResponseEntity.ok().build();
    }
}
