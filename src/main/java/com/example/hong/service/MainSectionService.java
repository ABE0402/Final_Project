package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.dto.CardDto;
import com.example.hong.dto.TagSectionDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Tag;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainSectionService {

    private final TagRepository tagRepository;
    private final CafeRepository cafeRepository;

    public List<TagSectionDto> fetchSections(String category, String sort, int page, String onlyTag) {

        // 1) 이 페이지에 노출할 태그들 (페이지당 4개 섹션)
        List<Tag> tags = (onlyTag != null && !onlyTag.isBlank())
                ? tagRepository.findByCategoryAndName(category, onlyTag)
                .map(List::of).orElse(List.of())
                : tagRepository
                .findByCategoryOrderByNameAsc(category, PageRequest.of(page, 4))
                .getContent(); // ★ Page -> List

        // 2) 각 태그별 카드 최대 N개(예: 12개) + 정렬 반영
        List<TagSectionDto> sections = new ArrayList<>();
        for (Tag t : tags) {
            List<Cafe> cafes = findCafesByTagAndSort(t, sort, 12); // ★ List<Cafe>

            List<CardDto> cards = cafes.stream().map(c ->
                    CardDto.builder()
                            .id(c.getId())
                            .type("CAFE")
                            .pathSegment("cafes")
                            .name(c.getName())
                            .address(nvl(c.getAddressRoad()))
                            .heroImageUrl(c.getHeroImageUrl())
                            .averageRating(c.getAverageRating() == null ? 0.0 : c.getAverageRating().doubleValue())
                            .reviewCount(c.getReviewCount())
                            .build()
            ).collect(Collectors.toList());

            sections.add(
                    TagSectionDto.builder()
                            .tag(t.getName())
                            .title("# " + t.getName())
                            .sortLabel(sortLabel(sort))
                            .sortSelected(Map.of(
                                    "recommend", "recommend".equals(sort),
                                    "rating",    "rating".equals(sort),
                                    "review",    "review".equals(sort),
                                    "favorite",  "favorite".equals(sort)
                            ))
                            .items(cards)
                            .build()
            );
        }
        return sections;
    }

    /** 태그 + 정렬 기준으로 카페 상위 N개 조회 */
    private List<Cafe> findCafesByTagAndSort(Tag tag, String sort, int limit) {
        // sort: recommend | rating | review | favorite
        Sort s = switch ((sort == null ? "recommend" : sort).toLowerCase()) {
            case "rating"   -> Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("reviewCount"));
            case "review"   -> Sort.by(Sort.Order.desc("reviewCount"), Sort.Order.desc("averageRating"));
            case "favorite" -> Sort.by(Sort.Order.desc("favoritesCount"), Sort.Order.desc("averageRating"));
            default         -> Sort.by(
                    Sort.Order.desc("averageRating"),
                    Sort.Order.desc("reviewCount"),
                    Sort.Order.desc("favoritesCount")
            );
        };

        Pageable pageable = PageRequest.of(0, limit, s);

        // ★ 레포지토리 메서드에서 Page<Cafe> 반환 → getContent()로 List<Cafe>
        return cafeRepository
                .findByApprovalStatusAndIsVisibleAndCafeTags_Tag_Id(
                        ApprovalStatus.APPROVED, true, tag.getId(), pageable
                )
                .getContent();
    }

    private String sortLabel(String sort) {
        return switch (sort == null ? "recommend" : sort) {
            case "rating"   -> "평점순";
            case "review"   -> "리뷰순";
            case "favorite" -> "즐겨찾기순";
            default         -> "추천순";
        };
    }

    private String nvl(String s) { return s == null ? "" : s; }
}
