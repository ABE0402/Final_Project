package com.example.hong.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FavoriteItemDto {
    Long cafeId;
    String name;
    String address;
    String heroImageUrl; // null일 수 있음
    Double averageRating;
    int reviewCount;
}