package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.dto.PlaceCardDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Restaurant;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PlaceQueryService {
    private final CafeRepository cafeRepository;
    private final RestaurantRepository restaurantRepository;


    public List<PlaceCardDto> topCards() {
        var cafes = cafeRepository
                .findTop8ByApprovalStatusAndIsVisibleOrderByAverageRatingDescReviewCountDesc(
                        ApprovalStatus.APPROVED, true)
                .stream().map(this::toDto);

        var rests = restaurantRepository
                .findTop8ByApprovalStatusAndIsVisibleOrderByAverageRatingDescReviewCountDesc(
                        ApprovalStatus.APPROVED, true)
                .stream().map(this::toDto);

        // 평점 DESC, 그 다음 리뷰수 DESC
        Comparator<PlaceCardDto> byRatingDesc = Comparator.comparingDouble(PlaceCardDto::getAverageRating).reversed();
        Comparator<PlaceCardDto> byReviewDesc = Comparator.comparingInt(PlaceCardDto::getReviewCount).reversed();

        return Stream.concat(cafes, rests)
                .sorted(Comparator.comparing(PlaceCardDto::getAverageRating).reversed()
                        .thenComparing(PlaceCardDto::getReviewCount).reversed())
                .limit(12)
                .toList();
    }

    private PlaceCardDto toDto(Cafe c) {
        return PlaceCardDto.builder()
                .type("CAFE")
                .id(c.getId())
                .name(c.getName())
                .address(c.getAddressRoad())
                .heroImageUrl(c.getHeroImageUrl())
                .averageRating(d(c.getAverageRating())) // BigDecimal -> double
                .reviewCount(c.getReviewCount())
                .build();
    }


    private PlaceCardDto toDto(Restaurant r) {
        return PlaceCardDto.builder()
                .type("RESTAURANT").id(r.getId()).name(r.getName())
                .address(r.getAddressRoad())
                .heroImageUrl(r.getHeroImageUrl())
                .averageRating(r.getAverageRating())
                .reviewCount(r.getReviewCount())
                .build();
    }

    private static double d(BigDecimal v) {
        return v != null ? v.doubleValue() : 0.0;
    }
}
