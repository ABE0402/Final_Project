package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.dto.PlaceCardDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Restaurant;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PlaceQueryService {

    private final CafeRepository cafeRepository;
    private final RestaurantRepository restaurantRepository;

    /** 메인 카드 그리드 조회 (카테고리/정렬/페이지네이션 지원) */
    public List<PlaceCardDto> fetchCards(String category, String sort, int page, int size) {
        String cat = (category == null) ? "all" : category.toLowerCase();
        String srt = (sort == null) ? "recommend" : sort.toLowerCase();
        Pageable pageable = PageRequest.of(page, size, sortFor(srt));

        if ("cafe".equals(cat)) {
            Page<Cafe> p = cafeRepository.findByApprovalStatusAndIsVisible(
                    ApprovalStatus.APPROVED, true, pageable);
            return p.stream().map(this::toDto).toList();

        } else if ("restaurant".equals(cat)) {
            Page<Restaurant> p = restaurantRepository.findByApprovalStatusAndIsVisible(
                    ApprovalStatus.APPROVED, true, pageable);
            return p.stream().map(this::toDto).toList();

        } else { // all: 두 엔티티를 합쳐 정렬 후 페이징 흉내 (간단 병합 전략)
            // 각 카테고리에서 size만큼 뽑아 합친 뒤 정렬 → 상위 size만 반환
            var cafes = cafeRepository.findByApprovalStatusAndIsVisible(
                            ApprovalStatus.APPROVED, true, PageRequest.of(page, size, sortFor(srt)))
                    .stream().map(this::toDto).toList();

            var rests = restaurantRepository.findByApprovalStatusAndIsVisible(
                            ApprovalStatus.APPROVED, true, PageRequest.of(page, size, sortFor(srt)))
                    .stream().map(this::toDto).toList();

            List<PlaceCardDto> merged = new ArrayList<>(cafes.size() + rests.size());
            merged.addAll(cafes);
            merged.addAll(rests);

            // recommend: 평점 desc, 리뷰 desc
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
        // recommend: avg desc, reviews desc
        return switch (sort) {
            case "rating" -> Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("reviewCount"));
            case "review" -> Sort.by(Sort.Order.desc("reviewCount"), Sort.Order.desc("averageRating"));
            default -> Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("reviewCount"));
        };
    }

    private PlaceCardDto toDto(Cafe c) {
        return PlaceCardDto.builder()
                .type("CAFE")
                .pathSegment("cafes")
                .id(c.getId())
                .name(c.getName())
                .address(c.getAddressRoad())
                .heroImageUrl(c.getHeroImageUrl())
                .averageRating(toDouble(c.getAverageRating())) // BigDecimal -> double (null 안전)
                .reviewCount(c.getReviewCount())
                .build();
    }

    private PlaceCardDto toDto(Restaurant r) {
        return PlaceCardDto.builder()
                .type("RESTAURANT")
                .pathSegment("restaurants")
                .id(r.getId())
                .name(r.getName())
                .address(r.getAddressRoad())
                .heroImageUrl(r.getHeroImageUrl())
                .averageRating(sanitize(r.getAverageRating())) // primitive double 그대로 사용
                .reviewCount(r.getReviewCount())
                .build();
    }

    // ===== helpers =====
    private static double toDouble(java.math.BigDecimal v) {
        return (v == null) ? 0.0 : v.doubleValue();
    }

    private static double sanitize(double v) {
        // 필요하면 NaN/무한대 방어
        return (Double.isNaN(v) || Double.isInfinite(v)) ? 0.0 : v;
    }
}
