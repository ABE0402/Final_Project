package com.example.hong.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ReviewItemDto {
    Long id;
    int rating;
    String content;
    String createdAt;   // 포맷된 문자열(예: 2025-09-19 17:30)
    String nickname;    // ★ 항상 채워서 전달

    String imageUrl1;
    String imageUrl2;
    String imageUrl3;
    String imageUrl4;
    String imageUrl5;
    List<AspectScoreDto> aspectScores; // 감성 분석 점수 리스트
}