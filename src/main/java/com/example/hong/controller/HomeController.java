// src/main/java/com/example/HONGAROUND/controller/HomeController.java
package com.example.hong.controller;

import com.example.hong.service.PlaceQueryService;
import com.example.hong.service.PlaceQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PlaceQueryService placeQueryService;

    // 화면 최초 진입
    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "all") String category,   // all | cafe | restaurant
                       @RequestParam(defaultValue = "recommend") String sort,  // recommend | rating | review
                       Model model) {
        int page = 0;
        int size = 12; // 첫 페이지 카드 개수 (그리드 3x4 등)

        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        model.addAttribute("isCafe", "cafe".equalsIgnoreCase(category));
        model.addAttribute("isRestaurant", "restaurant".equalsIgnoreCase(category));
        model.addAttribute("isAll", "all".equalsIgnoreCase(category));

        model.addAttribute("cards", placeQueryService.fetchCards(category, sort, page, size));
        return "main"; // templates/main.mustache
    }

    // 무한 스크롤/정렬/카테고리 전환시 카드 그리드 조각만 응답
    @GetMapping("/cards-fragment")
    public String cardsFragment(@RequestParam(defaultValue = "all") String category,
                                @RequestParam(defaultValue = "recommend") String sort,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "12") int size,
                                Model model) {
        model.addAttribute("cards", placeQueryService.fetchCards(category, sort, page, size));
        return "fragments/cards_grid"; // templates/fragments/cards_grid.mustache
    }
}
