// src/main/java/com/example/hong/service/MainSectionService.java
package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.domain.TagAppliesTo;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Tag;
import com.example.hong.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainSectionService {

    private final TagRepository tagRepository;
    private final CafeTagRepository cafeTagRepository;
    private final CafeRepository cafeRepository;
    private final ReviewRepository reviewRepository;

    private static final int TAGS_PER_PAGE = 4;

    /** ✅ 다중 카테고리(type → mood 순)로 섹션을 합쳐서 페이징 */
    public List<Map<String, Object>> fetchSectionsCombined(List<String> tagCategories, String sort, int page, String place) {
        var scopes = scopesFor(place);

        // 1) 카테고리별 태그 리스트 조회 후 순서대로 합치기
        List<Tag> combined = new ArrayList<>();
        for (String cat : tagCategories) {
            combined.addAll(tagRepository.findByCategoryAndAppliesToInOrderByDisplayOrderAscNameAsc(cat, scopes));
        }

        // 2) 페이지 슬라이스
        int from = page * TAGS_PER_PAGE;
        int to = Math.min(combined.size(), from + TAGS_PER_PAGE);
        if (from >= to) return List.of();

        List<Tag> pageTags = combined.subList(from, to);

        // 3) 각 태그 → 아이템(현재 카페 중심) 구성
        List<Map<String, Object>> sections = new ArrayList<>();
        for (Tag t : pageTags) {
            List<Map<String, Object>> items = new ArrayList<>();

            // place가 restaurant가 아닌 경우: 카페 아이템 채우기
            if (!"restaurant".equalsIgnoreCase(place)) {
                List<Long> cafeIds = cafeTagRepository.findCafeIdsByTagId(t.getId());
                if (!cafeIds.isEmpty()) {
                    var cafes = cafeRepository.findByIdInAndApprovalStatusAndIsVisible(
                            cafeIds, ApprovalStatus.APPROVED, true
                    );
                    var reviewCounts = reviewCountMap(cafeIds);

                    Comparator<Cafe> cmp = switch (sort == null ? "recommend" : sort) {
                        case "rating" -> Comparator
                                .comparing((Cafe c) -> dec(c.getAverageRating())).reversed()
                                .thenComparing(c -> reviewCounts.getOrDefault(c.getId(), 0), Comparator.reverseOrder());
                        case "review" -> Comparator
                                .comparing((Cafe c) -> reviewCounts.getOrDefault(c.getId(), 0), Comparator.reverseOrder())
                                .thenComparing((Cafe c) -> dec(c.getAverageRating()), Comparator.reverseOrder());
                        default -> Comparator
                                .comparing((Cafe c) -> dec(c.getAverageRating())).reversed()
                                .thenComparing(c -> reviewCounts.getOrDefault(c.getId(), 0), Comparator.reverseOrder());
                    };
                    cafes.sort(cmp);

                    int limit = Math.min(12, cafes.size());
                    for (int i = 0; i < limit; i++) {
                        Cafe c = cafes.get(i);
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("pathSegment", "cafes");
                        m.put("id", c.getId());
                        m.put("heroImageUrl", c.getHeroImageUrl());
                        m.put("name", c.getName());
                        m.put("address", c.getAddressRoad());
                        m.put("averageRating", str(c.getAverageRating()));
                        m.put("reviewCount", reviewCounts.getOrDefault(c.getId(), 0));
                        m.put("type", "카페");
                        items.add(m);
                    }
                }
            }

            // (추가 예정) place가 cafe가 아닌 경우엔 식당 아이템도 같은 방식으로 append

            // 아이템 없으면 섹션 스킵
            if (items.isEmpty()) continue;

            Map<String, Object> section = new LinkedHashMap<>();
            section.put("tag", t.getName());
            section.put("title", t.getName());
            section.put("items", items);

            Map<String,Object> sortSelected = Map.of(
                    "recommend", "recommend".equals(sort),
                    "rating", "rating".equals(sort),
                    "review", "review".equals(sort),
                    "favorite", "favorite".equals(sort)
            );
            section.put("sortSelected", sortSelected);

            section.put("sortLabel", switch (sort == null ? "recommend" : sort) {
                case "rating" -> "평점순";
                case "review" -> "리뷰순";
                case "favorite" -> "즐겨찾기순";
                default -> "추천순";
            });

            sections.add(section);
        }
        return sections;
    }

    private static List<TagAppliesTo> scopesFor(String place) {
        if ("cafe".equalsIgnoreCase(place)) {
            return List.of(TagAppliesTo.CAFE, TagAppliesTo.BOTH);
        } else if ("restaurant".equalsIgnoreCase(place)) {
            return List.of(TagAppliesTo.RESTAURANT, TagAppliesTo.BOTH);
        } else {
            return List.of(TagAppliesTo.CAFE, TagAppliesTo.RESTAURANT, TagAppliesTo.BOTH);
        }
    }
    private Map<Long, Integer> reviewCountMap(List<Long> cafeIds) {
        if (cafeIds == null || cafeIds.isEmpty()) return Collections.emptyMap();
        return reviewRepository.countByCafeIds(cafeIds).stream()
                .collect(Collectors.toMap(
                        ReviewRepository.CafeReviewCount::getCafeId,
                        rc -> Math.toIntExact(rc.getCnt())
                ));
    }
    private static BigDecimal dec(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private static String str(BigDecimal v) { return v == null ? "0.0" : v.toPlainString(); }
}
