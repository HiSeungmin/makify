package com.xladmt.makify.verification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class VerificationController {

    @GetMapping("/verify")
    public String check() {
        return "verify";
    }
}
