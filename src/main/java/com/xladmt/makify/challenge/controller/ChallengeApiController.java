package com.xladmt.makify.challenge.controller;

import com.xladmt.makify.challenge.service.ChallengeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeApiController {
    private ChallengeService challengeService;

//    @GetMapping
//    public List<ChallengeDto> getAllChallenges() {
//        return challengeService.findAll();
//    }
}
