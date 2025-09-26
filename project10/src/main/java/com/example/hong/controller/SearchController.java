package com.example.hong.controller;

import com.example.hong.dto.CafeDto;

import com.example.hong.entity.User;
import com.example.hong.repository.UserRepository;
import com.example.hong.service.SearchService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private  final UserRepository userRepository;

    @GetMapping("/search")
    public String searchpage() {
        return "search/search";
    }

    @GetMapping("/searchresult")
    public String searchResult(@RequestParam String query, Model model) {
        Long testUserId = 1L; // 임시 유저
        List<CafeDto> cafes = searchService.searchCafes(query, testUserId);

        List<Map<String, Object>> cards = cafes.stream().map(c -> Map.of(
                "id", c.getId(),
                "name", c.getName(),
                "type", "CAFE",
                "address", c.getAddressRoad(),
                "averageRating", c.getAverageRating(),
                "reviewCount", c.getReviewCount(),
                "heroImageUrl", c.getHeroImageUrl() != null ? c.getHeroImageUrl() : "/images/default_cafe.jpg",
                "menus", c.getMenus().stream()
                        .filter(m -> m.getName().toLowerCase().contains(query.toLowerCase()))
                        .map(m -> Map.of("name", m.getName(), "price", m.getPrice()))
                        .toList()
        )).toList();

        model.addAttribute("cards", cards);
        model.addAttribute("query", query);
        model.addAttribute("popularKeywords", searchService.getTopKeywords());

        return "search/searchresult";
    }

//        // 1. 테스트용 유저 가져오기 (DB에 미리 넣은 id=1 유저)
//        User testUser = userRepository.findById(1L).orElseThrow();
//
//        // 2. 검색 로그 저장
//        searchService.saveSearchLog(testUser, query);
//
//        // 3. 검색 결과 조회 (카페 + 메뉴 필터링 포함)
//        List<CafeDto> cafes = searchService.searchCafes(query);
//
//        // 4. 모델에 추가 (Mustache용 섹션 구성)
//        List<Map<String, Object>> cards = cafes.stream().map(c -> Map.of(
//                "id", c.getId(),
//                "name", c.getName(),
//                "type", "CAFE",
//                "address", c.getAddressRoad(),
//                "averageRating", c.getAverageRating(),
//                "reviewCount", c.getReviewCount(),
//                "heroImageUrl", c.getHeroImageUrl() != null ? c.getHeroImageUrl() : "/images/default_cafe.jpg",
//                "menus", c.getMenus().stream()
//                        .filter(m -> m.getName().toLowerCase().contains(query.toLowerCase()))
//                        .map(m -> Map.of("name", m.getName(), "price", m.getPrice()))
//                        .toList()
//        )).toList();
//
//        model.addAttribute("cards", cards);
//        model.addAttribute("query", query);
//
//        // 5. 인기 검색어 추가 - 상위 5개
//        List<String> popularKeywords = searchService.getTopKeywords();
//        model.addAttribute("popularKeywords", popularKeywords);
//
//        return "search/searchresult";
//    }
}