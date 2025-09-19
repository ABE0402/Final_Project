package com.example.hong.dto;
import com.example.hong.entity.Cafe;
import java.math.BigDecimal;

// Java 14 이상이면 record를 사용하는 것이 매우 편리합니다.
public record CafeSummaryResponseDto(
        Long id,
        String name,
        String addressRoad,
        String heroImageUrl,
        BigDecimal averageRating,
        int reviewCount
) {
    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static CafeSummaryResponseDto from(Cafe cafe) {
        return new CafeSummaryResponseDto(
                cafe.getId(),
                cafe.getName(),
                cafe.getAddressRoad(),
                cafe.getHeroImageUrl(),
                cafe.getAverageRating(),
                cafe.getReviewCount()
        );
    }
}
