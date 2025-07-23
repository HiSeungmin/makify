package com.xladmt.makify.challenge.controller;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.dto.ChallengeDetailResponse;
import com.xladmt.makify.challenge.service.ChallengeServiceImpl;
import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.common.validator.ChallengeCreateRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.net.Authenticator;
import java.util.Enumeration;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChallengeController {

    private final ChallengeServiceImpl challengeService;
    private final ChallengeCreateRequestValidator challengeCreateRequestValidator;

    @GetMapping("/challenges")
    public String getChallenges(Model model) {
        List<Challenge> challenges = challengeService.getAllVisibleChallenges();
        model.addAttribute("challenges", challenges);
        return "challenge/challenges";
    }

    @GetMapping("/challenges/{id}")
    public String getChallengeDetail(@PathVariable Long id, Model model, @AuthenticationPrincipal MemberDetails memberDetails) {
        String loginId = memberDetails.getMember().getLoginId();
        ChallengeDetailResponse challenge = challengeService.getChallenge(loginId, id);

        if (memberDetails != null) {
            model.addAttribute("loginMemberId", loginId);
        }

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

    @GetMapping("/challenges/{id}/join")
    public String showJoinPage(@PathVariable Long id, Model model) {
        // 1. 챌린지 조회
        Challenge challenge = challengeService.join(id);

        // 2. 모델에 담기
        model.addAttribute("challenge", challenge);

        // 3. 참여 페이지 반환
        return "challenge/join"; // templates/challenge/join.html
    }

}
