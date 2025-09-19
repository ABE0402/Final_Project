package com.example.mainproject.dto;

public record MyPlaceDto(
        Long id,
        String name,
        double lat,
        double lng
) {}
