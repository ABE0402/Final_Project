// src/main/java/com/example/hong/controller/HomeController.java
package com.example.hong.controller;

import com.example.hong.domain.TagAppliesTo;
import com.example.hong.dto.PlaceCardDto;
import com.example.hong.repository.TagRepository;
import com.example.hong.repository.UserRepository;
import com.example.hong.service.MainSectionService;
import com.example.hong.service.PlaceQueryService;
import com.example.hong.service.SegmentRecommendationService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PlaceQueryService placeQueryService;
    private final TagRepository tagRepository;
    private final MainSectionService mainSectionService;

    private final SegmentRecommendationService segmentRecommendationService;
    private final UserRepository userRepository;

    // 첫 화면: 기본 카테고리 = cafe
    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "cafe") String category,   // cafe | restaurant
                       @RequestParam(defaultValue = "recommend") String sort,  // recommend | rating | review
                       Authentication auth,
                       Model model) {

        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        model.addAttribute("isCafe", "cafe".equalsIgnoreCase(category));
        model.addAttribute("isRestaurant", "restaurant".equalsIgnoreCase(category));

        // ① 개인화 섹션(연령 → 성별) — 카페일 때만 노출
        List<Map<String,Object>> sections = new ArrayList<>();
        if ("cafe".equalsIgnoreCase(category)) {
            currentUser(auth).ifPresent(user -> {
                Map<String, List<PlaceCardDto>> rec = segmentRecommendationService.sectionsFor(user, 12);
                rec.forEach((title, cards) -> sections.add(toSection(title, cards)));
            });
        }

        // ② 태그 섹션(type → mood), page=0
        List<String> catList = List.of("type", "mood");
        int page = 0;
        int tagsPerPage = 4;
        var scopes = scopesFor(category);
        int totalTags = totalTagsFor(catList, scopes);

        var tagSections = mainSectionService.fetchSectionsCombined(catList, sort, page, category);
        sections.addAll(tagSections);

        boolean hasMore = (page + 1) * tagsPerPage < totalTags;

        model.addAttribute("sections", sections);
        model.addAttribute("hasMore", hasMore);
        return "main";
    }

    // 개인화 섹션 조각
    @GetMapping("/segments-fragment")
    public String segmentsFragment(@RequestParam(defaultValue = "cafe") String place, // 기본 'cafe'
                                   Authentication auth,
                                   Model model) {
        List<Map<String,Object>> sections = new ArrayList<>();
        if ("cafe".equalsIgnoreCase(place)) {
            currentUser(auth).ifPresent(user -> {
                Map<String, List<PlaceCardDto>> rec = segmentRecommendationService.sectionsFor(user, 12);
                rec.forEach((title, cards) -> sections.add(toSection(title, cards)));
            });
        }
        model.addAttribute("sections", sections);
        model.addAttribute("hasMore", true); // 무한스크롤은 태그에서만 판단
        return "fragments/fragment";
    }

    // 태그 조각 (type,mood)
    @GetMapping("/tags-fragment")
    public String tagsFragment(@RequestParam(required = false) String category,
                               @RequestParam(required = false) String categories, // "type,mood"
                               @RequestParam(defaultValue="cafe") String place,   // 기본 'cafe'
                               @RequestParam(defaultValue="recommend") String sort,
                               @RequestParam(defaultValue="0") int page,
                               Model model) {

        List<String> cats;
        if (categories != null && !categories.isBlank()) {
            cats = Arrays.stream(categories.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();
        } else if (category != null && !category.isBlank()) {
            cats = List.of(category.trim());
        } else {
            cats = List.of("type", "mood");
        }

        int tagsPerPage = 4;
        var scopes = scopesFor(place);
        int totalTags = totalTagsFor(cats, scopes);

        var sections = mainSectionService.fetchSectionsCombined(cats, sort, page, place);
        boolean hasMore = (page + 1) * tagsPerPage < totalTags;

        model.addAttribute("sections", sections);
        model.addAttribute("hasMore", hasMore);
        return "fragments/fragment";
    }

    /* ===== helpers ===== */

    private Optional<com.example.hong.entity.User> currentUser(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal p)) return Optional.empty();
        return userRepository.findById(p.getId());
    }

    private int totalTagsFor(List<String> categories, List<TagAppliesTo> scopes) {
        int sum = 0;
        for (String cat : categories) {
            sum += tagRepository.findByCategoryAndAppliesToInOrderByDisplayOrderAscNameAsc(cat, scopes).size();
        }
        return sum;
    }

    // 'all' 제거: 카페/식당만 처리, 그 외 값 들어오면 카페로 폴백
    private static List<TagAppliesTo> scopesFor(String place) {
        if ("restaurant".equalsIgnoreCase(place)) {
            return List.of(TagAppliesTo.RESTAURANT, TagAppliesTo.BOTH);
        }
        // default = cafe
        return List.of(TagAppliesTo.CAFE, TagAppliesTo.BOTH);
    }

    // 개인화 PlaceCardDto → fragment.mustache 섹션 형태로 변환
    private Map<String,Object> toSection(String title, List<PlaceCardDto> cards) {
        Map<String,Object> section = new LinkedHashMap<>();
        section.put("tag", title);
        section.put("title", title);
        List<Map<String,Object>> items = new ArrayList<>();
        for (PlaceCardDto c : cards) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("pathSegment", c.getPathSegment());
            m.put("id", c.getId());
            m.put("heroImageUrl", c.getHeroImageUrl());
            m.put("name", c.getName());
            m.put("address", c.getAddress());
            m.put("averageRating", c.getAverageRating());
            m.put("reviewCount", c.getReviewCount());
            m.put("type", "카페");
            items.add(m);
        }
        section.put("items", items);
        section.put("sortLabel", "추천순");
        section.put("sortSelected", Map.of("recommend", true, "rating", false, "review", false, "favorite", false));
        return section;
    }
}
