package com.example.hong.controller;

import com.example.hong.dto.SignupRequestDto;
import com.example.hong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value="error", required=false) String error,
                            @RequestParam(value="success", required=false) String success,
                            Model model) {
        if (error != null)   model.addAttribute("loginError", true);
        if (success != null) model.addAttribute("signupSuccess", true);
        return "login"; // templates/login.mustache
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupRequest", new SignupRequestDto());
        return "signup"; // templates/signup.mustache
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupRequestDto req, Model model) {
        try {
            userService.signup(req);
            return "redirect:/login?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "signup";
        }
    }
}
