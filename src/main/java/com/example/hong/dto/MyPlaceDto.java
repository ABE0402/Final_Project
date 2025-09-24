package com.example.hong.dto;

public record MyPlaceDto(
        Long id,
        String name,
        double lat,
        double lng
) {}