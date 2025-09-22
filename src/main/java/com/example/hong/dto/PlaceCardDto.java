package com.example.hong.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceCardDto {
    private String type;        // "CAFE" or "RESTAURANT"
    private Long id;
    private String name;
    private String address;
    private String heroImageUrl;
    private double averageRating;
    private int reviewCount;

    // ★ 무스타치에서 {{pathSegment}}로 사용
    public String getPathSegment() {
        return "CAFE".equalsIgnoreCase(type) ? "cafes" : "restaurants";
    }
}
