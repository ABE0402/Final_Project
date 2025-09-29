package com.example.hong.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerReviewDto {
    private Long reviewId;
    private String targetType;   // "CAFE" | "RESTAURANT"
    private String targetName;   // 매장명
    private Integer rating;
    private String content;
    private String createdAt;
    private String authorName;

    private Long replyId;          // null이면 미답글
    private String replyContent;   // 답글 내용
    private String replyUpdatedAt; // 수정일
    private boolean hasReply;
}
