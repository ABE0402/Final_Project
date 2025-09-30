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
    String createdAt;
    String nickname;

    String imageUrl1;
    String imageUrl2;
    String imageUrl3;
    String imageUrl4;
    String imageUrl5;
    List<AspectScoreDto> aspectScores;

    // [추가됨] 점주 답글 정보를 담을 필드
    String replyContent;
    String replyUpdatedAt;
}
