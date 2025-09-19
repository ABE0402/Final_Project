package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long placeId;
    private String reviewer;
    private String content;
    private String date; // 이 필드를 추가합니다.
    private String reviewImage; // 리뷰 사진 필드도 추가합니다.
}