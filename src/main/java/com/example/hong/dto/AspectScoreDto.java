package com.example.hong.dto;

import java.math.BigDecimal;

// Java 14 이상이면 record 사용 추천
public record AspectScoreDto(
        String aspect,
        BigDecimal score
) {}