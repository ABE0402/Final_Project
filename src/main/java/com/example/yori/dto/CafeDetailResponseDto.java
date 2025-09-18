package com.example.yori.dto;

import com.example.yori.entity.Cafe;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public record CafeDetailResponseDto(
        Long id,
        String name,
        String phone,
        String addressRoad,
        String description,
        String heroImageUrl,
        BigDecimal averageRating,
        int reviewCount,
        int favoritesCount,
        List<String> tags // 이 카페에 달린 태그 이름 목록
) {
    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static CafeDetailResponseDto from(Cafe cafe) {
        // Cafe 엔티티에 연결된 CafeTag 목록에서 Tag의 이름만 추출하여 리스트로 만듭니다.
        List<String> tagNames = cafe.getCafeTags().stream()
                .map(cafeTag -> cafeTag.getTag().getName())
                .collect(Collectors.toList());

        return new CafeDetailResponseDto(
                cafe.getId(),
                cafe.getName(),
                cafe.getPhone(),
                cafe.getAddressRoad(),
                cafe.getDescription(),
                cafe.getHeroImageUrl(),
                cafe.getAverageRating(),
                cafe.getReviewCount(),
                cafe.getFavoritesCount(),
                tagNames // 추출한 태그 이름 리스트
        );
    }
}