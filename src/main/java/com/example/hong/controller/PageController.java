package com.example.hong.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @Value("${kakao.js-key}")
    private String kakaoJsKey;

    @GetMapping("/pages/searchMap")
    public String searchMap(Model model) {
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        return "pages/searchMap"; // templates/pages/searchMap.mustache
    }
}
