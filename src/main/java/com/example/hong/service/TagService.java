// src/main/java/com/example/hong/service/TagService.java
package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.dto.CardDto;
import com.example.hong.dto.TagSectionDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Tag;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.CafeTagRepository;
import com.example.hong.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final CafeRepository cafeRepository;
    private final CafeTagRepository cafeTagRepository;

    public List<TagSectionDto> getTagSectionsByCategoryPageAndSort(String category, int page, String sort) {
        int tagsPerPage = 5; // 한 번에 섹션 5개씩
        Page<Tag> tagPage = tagRepository.findByCategoryOrderByNameAsc(
                category, PageRequest.of(page, tagsPerPage)
        );

        return tagPage.getContent().stream()
                .map(tag -> {

                    List<Cafe> cafes = findCafesByTagAndSort(tag, sort, 8);


                    Map<Long, String> hashtagsMap = buildHashtagsMap(cafes);

                    var cards = cafes.stream()
                            .map(c -> toCard(c, hashtagsMap.get(c.getId())))
                            .toList();

                    return TagSectionDto.builder()
                            .tag(tag.getName())
                            .category(category)
                            .items(cards)
                            .build();
                })
                .toList();
    }

    public List<TagSectionDto> getTagSectionByTagAndSort(String category, String tagName, String sort) {
        Tag tag = tagRepository.findByCategoryAndName(category, tagName)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다: " + tagName));

        List<Cafe> cafes = findCafesByTagAndSort(tag, sort, 8);
        Map<Long, String> hashtagsMap = buildHashtagsMap(cafes);

        var cards = cafes.stream()
                .map(c -> toCard(c, hashtagsMap.get(c.getId())))
                .toList();

        return List.of(TagSectionDto.builder()
                .tag(tag.getName())
                .category(category)
                .items(cards)
                .build());
    }


    private List<Cafe> findCafesByTagAndSort(Tag tag, String sort, int limit) {
        Sort order = switch (safe(sort)) {
            case "rating"   -> Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("reviewCount"));
            case "review"   -> Sort.by(Sort.Order.desc("reviewCount"),   Sort.Order.desc("averageRating"));
            case "favorite" -> Sort.by(Sort.Order.desc("favoritesCount"), Sort.Order.desc("averageRating"));
            default /* recommend */ -> Sort.by(
                    Sort.Order.desc("averageRating"),
                    Sort.Order.desc("reviewCount"),
                    Sort.Order.desc("favoritesCount")
            );
        };

        Pageable pageable = PageRequest.of(0, limit, order);
        return cafeRepository.findByApprovalStatusAndIsVisibleAndCafeTags_Tag_Id(
                ApprovalStatus.APPROVED, true, tag.getId(), pageable
        ).getContent();
    }

    private Map<Long, String> buildHashtagsMap(List<Cafe> cafes) {
        if (cafes == null || cafes.isEmpty()) return Collections.emptyMap();

        List<Long> ids = cafes.stream().map(Cafe::getId).toList();


        List<CafeTagRepository.CafeIdTagName> rows = cafeTagRepository.findTagNamesByCafeIds(ids);

        Map<Long, List<String>> nameMap = rows.stream()
                .collect(Collectors.groupingBy(
                        CafeTagRepository.CafeIdTagName::getCafeId,
                        LinkedHashMap::new,
                        Collectors.mapping(CafeTagRepository.CafeIdTagName::getName, Collectors.toList())
                ));

        Map<Long, String> hashtags = new HashMap<>();
        for (Long id : ids) {
            List<String> names = nameMap.getOrDefault(id, List.of());
            String text = names.stream()
                    .map(n -> "#" + n.replaceAll("\\s+", "")) // 공백 제거 후 해시태그
                    .collect(Collectors.joining(" "));
            hashtags.put(id, text);
        }
        return hashtags;
    }

    private String safe(String s) { return (s == null || s.isBlank()) ? "recommend" : s.toLowerCase(); }

    private CardDto toCard(Cafe c, String hashtags) {
        return CardDto.builder()
                .id(c.getId())
                .type("CAFE")
                .name(c.getName())
                .address(c.getAddressRoad())
                .heroImageUrl(c.getHeroImageUrl())
                .averageRating(c.getAverageRating() == null ? 0.0 : c.getAverageRating().doubleValue())
                .reviewCount(c.getReviewCount())
                .pathSegment("cafes")
                .hashtags(hashtags)
                .build();
    }
}
