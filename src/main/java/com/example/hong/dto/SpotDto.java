package com.example.hong.dto;

import com.example.hong.entity.Cafe;
import lombok.Data;

@Data
public class SpotDto {
    private Long id;
    private String name;
    private String type; // CAFE or RESTAURANT
    private String address;
    private String heroImageUrl;
    private double averageRating;
    private int reviewCount;

    public static SpotDto fromCafe(Cafe cafe) {
        SpotDto dto = new SpotDto();
        dto.setId(cafe.getId());
        dto.setName(cafe.getName());
        dto.setType("CAFE");
        dto.setAddress(cafe.getAddressRoad());
        dto.setHeroImageUrl(cafe.getHeroImageUrl());
        dto.setAverageRating(cafe.getAverageRating().doubleValue());
        //dto.setAverageRating(cafe.getAverageRating());
        dto.setReviewCount(cafe.getReviewCount());
        return dto;
    }

//    public static SpotDto fromRestaurant(Restaurant r) {
//        SpotDto dto = new SpotDto();
//        dto.setId(r.getId());
//        dto.setName(r.getName());
//        dto.setType("RESTAURANT");
//        dto.setAddress(r.getAddressRoad());
//        dto.setHeroImageUrl(r.getHeroImageUrl());
//        dto.setAverageRating(r.getAverageRating());
//        dto.setReviewCount(r.getReviewCount());
//        return dto;
//    }
}
