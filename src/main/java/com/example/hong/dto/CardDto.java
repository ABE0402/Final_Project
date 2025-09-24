package com.example.hong.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CardDto {
    Long id;
    String type;          // CAFE / RESTAURANT
    String pathSegment;   // "cafes" / "restaurants"
    String name;
    String address;
    String heroImageUrl;
    double averageRating;
    int reviewCount;
}
