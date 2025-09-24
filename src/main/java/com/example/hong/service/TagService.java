// src/main/java/com/example/hong/service/TagService.java
package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.dto.CardDto;
import com.example.hong.dto.TagSectionDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Tag;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final CafeRepository cafeRepository;

    /** 메인: 페이지 단위(태그 1페이지= N개)로 섹션 리스트 */
    public List<TagSectionDto> getTagSectionsByCategoryPageAndSort(String category, int page, String sort) {
        int tagsPerPage = 5; // 한 번에 섹션 5개씩
        Page<Tag> tagPage = tagRepository.findByCategoryOrderByNameAsc(
                category, PageRequest.of(page, tagsPerPage)
        );

        return tagPage.getContent().stream()
                .map(tag -> TagSectionDto.builder()
                        .tag(tag.getName())
                        .category(category)
                        .items(findCafesByTagAndSort(tag, sort, 8)  // 섹션당 카드 8개
                                .stream().map(this::toCard).toList())
                        .build())
                .toList();
    }

    /** 메인: 단일 태그만 새로고침(정렬 변경 시) */
    public List<TagSectionDto> getTagSectionByTagAndSort(String category, String tagName, String sort) {
        Tag tag = tagRepository.findByCategoryAndName(category, tagName)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다: " + tagName));

        var cards = findCafesByTagAndSort(tag, sort, 8).stream()
                .map(this::toCard)
                .toList();

        return List.of(TagSectionDto.builder()
                .tag(tag.getName())
                .category(category)
                .items(cards)
                .build());
    }

    /* ================= 내부 ================= */

    private List<Cafe> findCafesByTagAndSort(Tag tag, String sort, int limit) {
        Sort order = switch (safe(sort)) {
            case "rating"   -> Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("reviewCount"));
            case "review"   -> Sort.by(Sort.Order.desc("reviewCount"),   Sort.Order.desc("averageRating"));
            case "favorite" -> Sort.by(Sort.Order.desc("favoritesCount"), Sort.Order.desc("averageRating"));
            default /* recommend */ ->
                    Sort.by(Sort.Order.desc("averageRating"),
                            Sort.Order.desc("reviewCount"),
                            Sort.Order.desc("favoritesCount"));
        };

        Pageable pageable = PageRequest.of(0, limit, order);
        return cafeRepository.findByApprovalStatusAndIsVisibleAndCafeTags_Tag_Id(
                ApprovalStatus.APPROVED, true, tag.getId(), pageable
        ).getContent();
    }

    private String safe(String s) { return (s == null || s.isBlank()) ? "recommend" : s.toLowerCase(); }

    private CardDto toCard(Cafe c) {
        return CardDto.builder()
                .id(c.getId())
                .type("CAFE")
                .name(c.getName())
                .address(c.getAddressRoad())
                .heroImageUrl(c.getHeroImageUrl())
                .averageRating(c.getAverageRating() == null ? 0.0 : c.getAverageRating().doubleValue())
                .reviewCount(c.getReviewCount())
                .pathSegment("cafes")
                .build();
    }
}
