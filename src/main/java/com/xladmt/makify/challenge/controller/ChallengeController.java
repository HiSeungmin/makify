package com.xladmt.makify.challenge.controller;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.service.ChallengeServiceImpl;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.validator.ChallengeCreateRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

   @GetMapping("/challenges/new")
    public String showCreateChallengeForm(Model model) {
        // 폼에서 사용할 기본 객체가 있다면 여기서 추가 가능
        // model.addAttribute("challengeForm", new ChallengeForm());
        return "challenge/create"; // templates/challenge/create.html
    }

    @PostMapping("/challenges/new")
    public String createChallenge(@ModelAttribute ChallengeCreateRequest request, BindingResult bindingResult, Model model) {
        challengeCreateRequestValidator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            // 에러 메시지 하나만 전달 (여러 개 하고 싶으면 리스트 처리)
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            model.addAttribute("errorMessage", errorMessage);

            return "challenge/create"; // 그대로 보여주되 기존 데이터는 유지됨
        }

        //        System.out.println(request.toString());
//        // 헤더 출력
//        Enumeration<String> headerNames = httpRequest.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            String headerValue = httpRequest.getHeader(headerName);
//            System.out.println("[Header] " + headerName + " : " + headerValue);
//        }

        challengeService.create(request);
        return "redirect:/challenges";
    }

}
