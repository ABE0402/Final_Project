package com.example.hong.controller;

import com.example.hong.dto.CafeSearchResultDto;
import com.example.hong.dto.SearchRequestDto;     // 요청을 담는 DTO
import com.example.hong.service.SearchService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.util.StringUtils;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public String search(
            @ModelAttribute SearchRequestDto searchRequest, // (1) @ModelAttribute로 모든 파라미터 받기
            @AuthenticationPrincipal AppUserPrincipal userPrincipal,
            Model model
    ) {
        // (2) 실제 로그인 사용자 ID 추출 (비로그인 시 null)
        Long userId = (userPrincipal != null) ? userPrincipal.getId() : null;

        // (3) 검색 조건이 하나라도 있는지 확인하는 헬퍼 메소드
        boolean hasSearchCriteria = hasSearchCriteria(searchRequest);

        if (hasSearchCriteria) {
            // --- 검색 조건이 있는 경우: 검색 결과를 보여줌 ---

            // (4) 통합된 서비스 메소드 호출
            List<CafeSearchResultDto> results = searchService.searchCafesAndLog(searchRequest, userId);

            // (5) DTO 리스트를 변환 없이 그대로 View에 전달
            model.addAttribute("results", results);
            model.addAttribute("searchParams", searchRequest); // 검색 조건 유지를 위해 DTO 전달

            return "search/searchresult"; // 결과 페이지로 이동

        } else {
            // --- 검색 조건이 없는 경우: 빈 검색 페이지를 보여줌 ---
            // (인기 검색어 등 초기 데이터를 여기서 전달 가능)
            // model.addAttribute("popularKeywords", searchService.getTopKeywords());
            return "search/search"; // 빈 검색 페이지로 이동
        }
    }

    // 검색 조건(텍스트 또는 태그)이 하나라도 있는지 확인하는 메소드
    private boolean hasSearchCriteria(SearchRequestDto request) {
        return StringUtils.hasText(request.getQuery()) ||
                StringUtils.hasText(request.getCompanion()) ||
                StringUtils.hasText(request.getMood()) ||
                StringUtils.hasText(request.getAmenities()) ||
                StringUtils.hasText(request.getReservation()) ||
                StringUtils.hasText(request.getDays()) ||
                StringUtils.hasText(request.getType());
    }
}