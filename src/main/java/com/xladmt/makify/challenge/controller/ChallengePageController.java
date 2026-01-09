package com.xladmt.makify.challenge.controller;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.dto.ChallengeDetailResponse;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.service.ChallengeServiceImpl;
import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.common.entity.Challenge;
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

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChallengePageController {

    private final ChallengeServiceImpl challengeService;
    private final ChallengeRepository challengeRepository;
    private final ChallengeValidator challengeValidator;
    private final ChallengeCreateRequestValidator challengeCreateRequestValidator;

    @GetMapping("/challenges")
    public String getChallenges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model
    ) {
        // 페이징 처리된 데이터 가져오기
        List<Challenge> allChallenges = challengeService.getAllVisibleChallenges();
        
        // 수동 페이징 처리
        int totalElements = allChallenges.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<Challenge> pagedChallenges = start < totalElements ? 
            allChallenges.subList(start, end) : new java.util.ArrayList<>();
        
        model.addAttribute("challenges", pagedChallenges);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("hasPrevious", page > 0);
        
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

        return "challenge/detail"; // detail.html
    }

    @GetMapping("/challenges/new")
    public String createChallengeForm() {
        return "challenge/create";
    }

    @PostMapping("/challenges/new")
    public String createChallenge(@ModelAttribute ChallengeCreateRequest request, @AuthenticationPrincipal MemberDetails memberDetails, BindingResult bindingResult) {

        log.info("챌린지 생성 중.. \n"+request);
        challengeCreateRequestValidator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            ErrorCode errorCode = challengeCreateRequestValidator.resolveErrorCode(bindingResult);
            throw new BusinessException(errorCode);
        }

        challengeService.create(request, memberDetails.getId());

        return "redirect:/challenges";
    }

    // 챌린지 참여 페이지 - 결제 정보만 표시 (DB 생성 X)
    @GetMapping("/challenges/{id}/join")
    public String showJoinPage(@PathVariable Long id, @AuthenticationPrincipal MemberDetails memberDetails, Model model) {
        // 1. 챌린지 조회
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        // 2. 참여 가능 여부 확인
        challengeValidator.validateJoinable(id, memberDetails.getId());
        
        // 3. 결제 정보만 준비 (uuid 없음)
        RequestPayDto paymentInfo = challengeService.getPaymentInfo(id, memberDetails.getId());
        
        // 4. 모델에 담기
        model.addAttribute("challenge", challenge);
        model.addAttribute("paymentInfo", paymentInfo);
        model.addAttribute("challengeId", id);
        model.addAttribute("userId", memberDetails.getId());
        
        // 5. 참여 페이지 반환
        return "challenge/join";
    }

}
