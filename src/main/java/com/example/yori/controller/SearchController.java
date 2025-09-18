package com.example.yori.controller;

import com.example.yori.dto.SearchRequestDto;
import com.example.yori.dto.CafeSummaryResponseDto; // 목록 표시에 사용할 DTO
import com.example.yori.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public String searchCafes(SearchRequestDto searchRequest, Model model) {
        // TODO: 로그인 기능 구현 후 실제 사용자 ID를 넘겨야 합니다. (현재는 임시 ID 1 사용)
        Long userId = 1L;

        // 1. Service를 호출하여 검색 결과와 로그 기록을 동시에 처리
        List<CafeSummaryResponseDto> results = searchService.searchCafesAndLog(searchRequest, userId);

        // 2. Model에 결과를 담아 View(HTML)로 전달
        model.addAttribute("cafes", results);
        model.addAttribute("searchParams", searchRequest); // 사용자가 검색한 조건을 화면에 유지하기 위함

        // 3. 검색 결과 페이지 템플릿 반환
        return "search/search"; // templates/search/search.html 파일을 반환
    }
}