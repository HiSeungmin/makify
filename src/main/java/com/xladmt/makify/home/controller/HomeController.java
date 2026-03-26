package com.xladmt.makify.home.controller;

import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.common.entity.Challenge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ChallengeRepository challengeRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<Challenge> homeChallenges = challengeRepository.findHomeChallenge(List.of(1L, 3L, 4L));
        model.addAttribute("homeChallenges", homeChallenges);
        return "home";
    }

    @GetMapping("/notifications")
    public String notifications() {
        return "notifications";
    }
}
