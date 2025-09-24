package com.example.hong.controller;

import com.example.hong.dto.TagSectionDto;
import com.example.hong.service.TagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
@Slf4j
public class MainPageController {

    private final TagService tagService;

    /**
     * 메인 페이지
     */
    @GetMapping("/main")
    public String mainPage(@RequestParam(defaultValue = "cafe") String category,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "recommend") String sort,
                           Model model) {

        List<TagSectionDto> tagSections = tagService.getTagSectionsByCategoryPageAndSort(category, page, sort);
        model.addAttribute("tagSections", tagSections);
        model.addAttribute("category", category);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSort", sort);

        return "main";
    }

    /**
     * 무한 스크롤 & 섹션별 정렬 지원
     */
    @GetMapping("/tags-fragment")
    public String loadMoreTags(@RequestParam String category,
                               @RequestParam(required = false) String tag,
                               @RequestParam int page,
                               @RequestParam(defaultValue = "recommend") String sort,
                               Model model) {

        log.info("loadMoreTags - category: {}, tag: {}, page: {}, sort: {}", category, tag, page, sort);
        log.info("tag : {}", tag);
        List<TagSectionDto> tagSections;

        if (tag != null && !tag.isBlank()) {
            // 특정 태그 섹션만 가져오기
            tagSections = tagService.getTagSectionByTagAndSort(category, tag, sort);
        } else {
            // 여러 태그 섹션 가져오기
            tagSections = tagService.getTagSectionsByCategoryPageAndSort(category, page, sort);
        }

        if (tagSections.isEmpty()) {
            return "fragments/empty";
        }

        model.addAttribute("tagSections", tagSections);
        model.addAttribute("currentSort", sort);
        model.addAttribute("category", category);
        return "fragments/fragment";
    }
    @GetMapping("/post")
    public String PostPage(){
        return "owner/shops_new";
    }
}
