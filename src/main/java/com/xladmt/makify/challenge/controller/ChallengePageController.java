package com.xladmt.makify.challenge.controller;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.dto.ChallengeDetailResponse;
import com.xladmt.makify.challenge.dto.ChallengePageDto;
import com.xladmt.makify.verification.repository.ChallengeRecordRepository;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import com.xladmt.makify.challenge.service.ChallengeServiceImpl;
import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.common.validator.ChallengeCreateRequestValidator;
import com.xladmt.makify.common.validator.ChallengeValidator;
import com.xladmt.makify.payment.dto.RequestPayDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChallengePageController {

    private final ChallengeServiceImpl challengeService;
    private final ChallengeRepository challengeRepository;
    private final ChallengeValidator challengeValidator;
    private final ChallengeCreateRequestValidator challengeCreateRequestValidator;
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRecordRepository challengeRecordRepository;

    @GetMapping("/challenges")
    public String getChallenges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model
    ) {

        List<Challenge> allChallenges = challengeService.getAllVisibleChallenges();

        ChallengePageDto challengePageDto = challengeService.getChallengesPage(allChallenges, page, size);

        model.addAttribute("challengePage", challengePageDto);
        
        return "challenge/challenges";
    }

    // 챌린지 상세 페이지
    @GetMapping("/challenges/{id}")
    public String getChallengeDetail(@PathVariable Long id, Model model, @AuthenticationPrincipal MemberDetails memberDetails) {
        String loginId = null;
        if (memberDetails != null) {
            loginId = memberDetails.getMember().getLoginId();
        }
        
        ChallengeDetailResponse challenge = challengeService.getChallenge(loginId, id);
        model.addAttribute("loginMemberId", loginId);
        model.addAttribute("challenge", challenge);

        return "challenge/detail";
    }

    @GetMapping("/challenges/new")
    public String createChallengeForm() {
        return "challenge/create";
    }

    @PostMapping("/challenges/new")
    public String createChallenge(@ModelAttribute ChallengeCreateRequest request, @AuthenticationPrincipal MemberDetails memberDetails, BindingResult bindingResult) {

        challengeCreateRequestValidator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            ErrorCode errorCode = challengeCreateRequestValidator.resolveErrorCode(bindingResult);
            throw new BusinessException(errorCode);
        }

        challengeService.create(request, memberDetails.getId());

        return "redirect:/challenges";
    }

    // 챌린지 인증 페이지
    @GetMapping("/challenges/{id}/verify")
    public String showVerifyPage(@PathVariable Long id, @AuthenticationPrincipal MemberDetails memberDetails, Model model) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        UserChallenge userChallenge = userChallengeRepository.findByMemberIdAndChallengeId(memberDetails.getId(), id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        int todayVerifiedCount = challengeRecordRepository.countTodayVerifications(
                memberDetails.getId(), id, LocalDate.now());

        model.addAttribute("challenge", challenge);
        model.addAttribute("verificationMethod", challenge.getVerificationMethod());
        model.addAttribute("targetFrequency", userChallenge.getTargetFrequency() == null ? 1 : userChallenge.getTargetFrequency());
        model.addAttribute("todayVerifiedCount", todayVerifiedCount);

        return "challenge/verify";
    }

    // 챌린지 참여 페이지 - 결제 정보만 표시
    @GetMapping("/challenges/{id}/join")
    public String showJoinPage(@PathVariable Long id, @AuthenticationPrincipal MemberDetails memberDetails, Model model) {
        // 1. 챌린지 조회
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        // 2. 참여 가능 여부 확인
        challengeValidator.validateJoinable(id, memberDetails.getId());
        
        // 3. 결제 정보
        RequestPayDto paymentInfo = challengeService.getPaymentInfo(id, memberDetails.getId());
        
        // 4. 모델에 담기
        model.addAttribute("challenge", challenge);
        model.addAttribute("paymentInfo", paymentInfo);
        model.addAttribute("challengeId", id);
        model.addAttribute("userId", memberDetails.getId());
        
        // 5. 참여 페이지 반환
        return "challenge/join";
    }


//    @GetMapping("/api/challenges/search")
//    public String searchChallenges(@RequestParam(required = false) String keyword,
//                                   @RequestParam(required = false) String category,
//                                   @RequestParam(required = false) String status,  // 상태 필터 추가
//                                   @RequestParam(defaultValue = "latest") String sortBy,
//                                   @RequestParam(defaultValue = "0") String page,
//                                   @RequestParam(defaultValue = "20") String size){
//
//
//        return "";
//    }

}
