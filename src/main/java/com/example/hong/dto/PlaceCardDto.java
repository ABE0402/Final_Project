package com.example.hong.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceCardDto {
    private String type;          // "CAFE" / "RESTAURANT"
    private String pathSegment;   // "cafes" / "restaurants"
    private Long id;
    private String name;
    private String address;
    private String heroImageUrl;
    private Double averageRating; // toDto에서 BigDecimal -> double 변환해서 세팅
    private Integer reviewCount;

    // ★ 무스타치에서 {{pathSegment}}로 사용
    public String getPathSegment() {
        return "CAFE".equalsIgnoreCase(type) ? "cafes" : "restaurants";
    }
}
