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
    public String showChallenges(Model model) {
        List<Challenge> challenges = challengeService.getAllVisibleChallenges();
        log.info(challenges.size()+" <--- 챌린지 수");
        model.addAttribute("challenges", challenges);
        return "challenge/challenges";
    }

    @GetMapping("/challenges/{id}")
    public String getChallengeDetail(@PathVariable Long id, Model model, @AuthenticationPrincipal MemberDetails memberDetails) {
        ChallengeDetailResponse challenge = challengeService.getChallenge(id);

        // 로그인한 사용자 ID 전달
        if (memberDetails != null) {
            model.addAttribute("loginMemberId", memberDetails.getMember().getLoginId());
        }

        model.addAttribute("challenge", challenge);
        return "challenge/detail"; // detail.html
    }

   @GetMapping("/challenges/new")
    public String showCreateChallengeForm(Model model) {
        // 폼에서 사용할 기본 객체가 있다면 여기서 추가 가능
        // model.addAttribute("challengeForm", new ChallengeForm());
        return "challenge/create"; // templates/challenge/create.html
    }

    @PostMapping("/challenges/new")
    public String createChallenge(@ModelAttribute ChallengeCreateRequest request, @AuthenticationPrincipal MemberDetails memberDetails, BindingResult bindingResult) {

//        System.out.println(request.toString());
//        // 헤더 출력
//        Enumeration<String> headerNames = httpRequest.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            String headerValue = httpRequest.getHeader(headerName);
//            System.out.println("[Header] " + headerName + " : " + headerValue);
//        }

        challengeCreateRequestValidator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            // 첫 번째 에러 메시지로 BusinessException 생성
            FieldError firstError = (FieldError) bindingResult.getAllErrors().get(0);
            String field = firstError.getField();

            ErrorCode errorCode = switch (field) {
                case "title" -> ErrorCode.CHALLENGE_TITLE_REQUIRED;
                case "description" -> ErrorCode.CHALLENGE_DESCRIPTION_REQUIRED;
                case "category" -> ErrorCode.CHALLENGE_CATEGORY_REQUIRED;
                case "endDate" -> ErrorCode.CHALLENGE_INVALID_DATE_RANGE;
                case "endTime" -> ErrorCode.CHALLENGE_INVALID_TIME_RANGE;
                case "privateCode" -> ErrorCode.CHALLENGE_PRIVATE_CODE_REQUIRED;
                case "fixedDeposit" -> ErrorCode.CHALLENGE_FIXED_DEPOSIT_REQUIRED;
                case "maxDeposit" -> ErrorCode.CHALLENGE_MAX_DEPOSIT_REQUIRED;
                case "minDailyCount" -> ErrorCode.CHALLENGE_MIN_DAILY_COUNT_REQUIRED;
                default -> ErrorCode.CHALLENGE_TITLE_REQUIRED; // fallback
            };

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
