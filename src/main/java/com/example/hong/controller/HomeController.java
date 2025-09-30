// src/main/java/com/example/HONGAROUND/controller/HomeController.java
package com.example.hong.controller;

import com.example.hong.repository.TagRepository;
import com.example.hong.service.MainSectionService;
import com.example.hong.service.PlaceQueryService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PlaceQueryService placeQueryService;
    private final TagRepository tagRepository;
    private final MainSectionService mainSectionService;

    // 화면 최초 진입
    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "all") String category,   // all | cafe | restaurant
                       @RequestParam(defaultValue = "recommend") String sort,  // recommend | rating | review
                       @AuthenticationPrincipal AppUserPrincipal userPrincipal,
                       Model model) {
        int page = 0;
        int size = 12; // 첫 페이지 카드 개수 (그리드 3x4 등)

        if (userPrincipal != null) {
            model.addAttribute("user", userPrincipal);
        }

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
    @GetMapping("/tags-fragment")
    public String tagsFragment(
            @RequestParam String category,
            @RequestParam(defaultValue="recommend") String sort,
            @RequestParam(defaultValue="0") int page,
            Model model) {

        int tagsPerPage = 4; // 한 번에 그릴 섹션 수
        int totalTags = tagRepository.findByCategoryOrderByNameAsc(category).size();

        var sections = mainSectionService.fetchSections(category, sort, page, null); // 위에서 만든 서비스

        boolean hasMore = (page+1) * tagsPerPage < totalTags;

        model.addAttribute("sections", sections);
        model.addAttribute("hasMore", hasMore);  // ← 프론트에서 보고 종료

        return "fragments/fragment"; // 섹션 카드들만 렌더하는 부분 템플릿
    }
}
