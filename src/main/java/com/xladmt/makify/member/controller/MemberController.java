package com.xladmt.makify.member.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberController {

    @GetMapping("/login")
    public String loginForm() {
        return "member/login"; // 로그인 폼
    }

//    @PostMapping("/login")
//    public void doLogin(@RequestParam String username,
//                          @RequestParam String password,
//                          HttpSession session, Model model) {
//
//        // [임시 로그인 로직] 아이디: test, 비번: 1234만 로그인 성공
////        if ("user1".equals(username) && "1234".equals(password)) {
////            session.setAttribute("loginUser", username);
////            return "redirect:/";
////        } else {
////            model.addAttribute("loginError", true);
////            return "member/login";
////        }
//        //return "redirect:/";
//    }
}
