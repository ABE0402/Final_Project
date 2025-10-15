package com.example.hong.controller;

import com.example.hong.document.PlaceDocument;
import com.example.hong.service.SearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // ObjectMapper import
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // Value import
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ElasticsearchController {

    private final SearchService searchService;
    private final ObjectMapper objectMapper; //  JSON 변환 하기 위해

    @Value("${kakao.js-key}")
    private String kakaoJsKey;

    @GetMapping("/elasticsearch")
    public String searchByElasticsearch(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) String category,
            Model model
    ) throws JsonProcessingException {
        List<PlaceDocument> results = Collections.emptyList();

        if (StringUtils.hasText(keyword) || StringUtils.hasText(category)) {
            results = searchService.searchByElasticsearch(keyword, category);
        }


        String resultsJson = objectMapper.writeValueAsString(results);
        model.addAttribute("resultsJson", resultsJson);

        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("kakaoJsKey", kakaoJsKey);

        return "search/elasticsearch_map_results";
    }
}

