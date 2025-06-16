package com.xladmt.makify.challenge.controller;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.service.ChallengeService;
import com.xladmt.makify.common.entity.Challenge;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChallengeController {

    private final ChallengeService challengeService;

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
    public String createChallenge(@ModelAttribute ChallengeCreateRequest request) {
        challengeService.create(request);
        return "redirect:challenge/challenges";
    }

}
