package com.example.hong.dto;

import com.example.hong.entity.Cafe;

import jakarta.persistence.Column;
import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CafeDto {
    private Long id;
    private String name;
    private String addressRoad;
    private double lat;
    private double lng;
    private double averageRating;
    private int reviewCount;
    private String heroImageUrl;
    private String approvalStatus;
    private String description;
    private int favoritesCount;
    private String phone;
    private String postcode;

    private List<MenuDto> menus;

    public static CafeDto fromEntity(Cafe cafe) {
        List<MenuDto> menuDto = cafe.getMenus() != null ?
                cafe.getMenus().stream().map(MenuDto::fromEntity).toList() : List.of();

        return CafeDto.builder()
                .id(cafe.getId())
                .name(cafe.getName())
                .addressRoad(cafe.getAddressRoad())
                .lat(cafe.getLat() != null ? cafe.getLat().doubleValue() : 0.0)
                .lng(cafe.getLng() != null ? cafe.getLng().doubleValue() : 0.0)
                .averageRating(cafe.getAverageRating() != null ? cafe.getAverageRating().doubleValue() : 0.0)
                .reviewCount(cafe.getReviewCount())
                .heroImageUrl(cafe.getHeroImageUrl())
                .approvalStatus(cafe.getApprovalStatus() != null ? cafe.getApprovalStatus().name() : null)
                .description(cafe.getDescription())
                .favoritesCount(cafe.getFavoritesCount())
                .phone(cafe.getPhone())
                .postcode(cafe.getPostcode())
                .menus(menuDto)
                .build();
    }
}