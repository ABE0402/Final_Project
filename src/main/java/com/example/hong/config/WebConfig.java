package com.example.hong.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class WebConfig {

    @Value("${kakao.js-key}")
    private String kakaoJsKey;

    @ModelAttribute
    public void addWebAttributes(Model model){
        model.addAttribute("kakaoJsKey", kakaoJsKey);
    }
}
