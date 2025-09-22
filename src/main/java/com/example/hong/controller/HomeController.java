package com.example.hong.controller;

import com.example.hong.service.PlaceQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final PlaceQueryService placeQueryService;


    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("cards", placeQueryService.topCards());
        return "main"; // templates/main.mustache
    }
}
