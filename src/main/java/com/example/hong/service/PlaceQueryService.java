package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.dto.PlaceCardDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Restaurant;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.CafeTagRepository;
import com.example.hong.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceQueryService {

    private final CafeRepository cafeRepository;
    private final RestaurantRepository restaurantRepository;
    private final CafeTagRepository cafeTagRepository; // ★ 추가

    /** 메인 카드 그리드 조회 (카테고리/정렬/페이지네이션 지원) */
    public List<PlaceCardDto> fetchCards(String category, String sort, int page, int size) {
        String cat = (category == null) ? "all" : category.toLowerCase();
        String srt = (sort == null) ? "recommend" : sort.toLowerCase();
        Pageable pageable = PageRequest.of(page, size, sortFor(srt));

        if ("cafe".equals(cat)) {
            Page<Cafe> p = cafeRepository.findByApprovalStatusAndIsVisible(
                    ApprovalStatus.APPROVED, true, pageable);

            // ★ cafe ids -> hashtags 맵
            Map<Long, String> hashtags = buildCafeHashtags(p.getContent());

            return p.stream().map(c -> toDto(c, hashtags.getOrDefault(c.getId(), ""))).toList();

        } else if ("restaurant".equals(cat)) {
            Page<Restaurant> p = restaurantRepository.findByApprovalStatusAndIsVisible(
                    ApprovalStatus.APPROVED, true, pageable);

            // ★ 레스토랑은 일단 빈 문자열
            return p.stream().map(r -> toDto(r, "")).toList();

        } else {
            // all: 카페/식당 각각 size만큼 가져와 합친 뒤 정렬, 상위 size 반환
            var cafePage = cafeRepository.findByApprovalStatusAndIsVisible(
                    ApprovalStatus.APPROVED, true, PageRequest.of(page, size, sortFor(srt)));
            var restPage = restaurantRepository.findByApprovalStatusAndIsVisible(
                    ApprovalStatus.APPROVED, true, PageRequest.of(page, size, sortFor(srt)));

            // ★ cafe hashtags
            Map<Long, String> hashtags = buildCafeHashtags(cafePage.getContent());

            List<PlaceCardDto> cafes = cafePage.stream()
                    .map(c -> toDto(c, hashtags.getOrDefault(c.getId(), "")))
                    .toList();

            List<PlaceCardDto> rests = restPage.stream()
                    .map(r -> toDto(r, "")) // 레스토랑은 빈 해시태그
                    .toList();

            List<PlaceCardDto> merged = new ArrayList<>(cafes.size() + rests.size());
            merged.addAll(cafes);
            merged.addAll(rests);

            Comparator<PlaceCardDto> cmp = Comparator
                    .comparing(PlaceCardDto::getAverageRating, Comparator.nullsFirst(Double::compareTo)).reversed()
                    .thenComparing(PlaceCardDto::getReviewCount, Comparator.nullsFirst(Integer::compareTo)).reversed();

            if ("rating".equals(srt)) {
                cmp = Comparator.comparing(PlaceCardDto::getAverageRating, Comparator.nullsFirst(Double::compareTo)).reversed();
            } else if ("review".equals(srt)) {
                cmp = Comparator.comparing(PlaceCardDto::getReviewCount, Comparator.nullsFirst(Integer::compareTo)).reversed();
            }
            return merged.stream().sorted(cmp).limit(size).toList();
        }
    }

    private Sort sortFor(String sort) {
        return switch (sort) {
            case "rating" -> Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("reviewCount"));
            case "review" -> Sort.by(Sort.Order.desc("reviewCount"), Sort.Order.desc("averageRating"));
            default -> Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("reviewCount"));
        };
    }

    // ======= DTO 변환 =======
    private PlaceCardDto toDto(Cafe c, String hashtags) {
        return PlaceCardDto.builder()
                .type("CAFE")
                .pathSegment("cafes")
                .id(c.getId())
                .name(c.getName())
                .address(c.getAddressRoad())
                .heroImageUrl(c.getHeroImageUrl())
                .averageRating(toDouble(c.getAverageRating()))
                .reviewCount(c.getReviewCount())
                .hashtags(hashtags) // ★ 세팅
                .build();
    }

    private PlaceCardDto toDto(Restaurant r, String hashtags) {
        return PlaceCardDto.builder()
                .type("RESTAURANT")
                .pathSegment("restaurants")
                .id(r.getId())
                .name(r.getName())
                .address(r.getAddressRoad())
                .heroImageUrl(r.getHeroImageUrl())
                .averageRating(toDouble(r.getAverageRating()))
                .reviewCount(r.getReviewCount())
                .hashtags(hashtags) // 현재는 ""
                .build();
    }

    // ======= 해시태그 유틸 =======
    private Map<Long, String> buildCafeHashtags(List<Cafe> cafes) {
        if (cafes == null || cafes.isEmpty()) return Collections.emptyMap();
        List<Long> cafeIds = cafes.stream().map(Cafe::getId).toList();

        // 프로젝션으로 (cafeId, tagName) 가져오기
        var rows = cafeTagRepository.findTagNamesByCafeIds(cafeIds);

        Map<Long, List<String>> byCafe = rows.stream().collect(
                Collectors.groupingBy(
                        CafeTagRepository.CafeIdTagName::getCafeId,
                        LinkedHashMap::new,
                        Collectors.mapping(CafeTagRepository.CafeIdTagName::getName, Collectors.toList())
                )
        );

        Map<Long, String> result = new HashMap<>();
        for (Long id : cafeIds) {
            String text = byCafe.getOrDefault(id, List.of()).stream()
                    .map(n -> "#" + n.replaceAll("\\s+", "")) // 공백 제거
                    .collect(Collectors.joining(" "));
            result.put(id, text);
        }
        return result;
    }

    // ===== helpers =====
    private static double toDouble(java.math.BigDecimal v) {
        return (v == null) ? 0.0 : v.doubleValue();
    }
    private static double sanitize(double v) { return (Double.isNaN(v) || Double.isInfinite(v)) ? 0.0 : v; }
}
