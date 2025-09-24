// src/main/java/.../dto/main/TagSectionDto.java
package com.example.hong.dto;

import com.example.hong.dto.CardDto;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class TagSectionDto {
    String tag;          // ex) "디저트", "노트북하기좋아요"
    String title;
    String category;  // 섹션 제목
    String sortLabel;    // 버튼에 표기할 현재 정렬라벨 (예: "추천순")
    Map<String, Boolean> sortSelected; // {recommend:true, rating:false, ...}

    @Singular("item")
    List<CardDto> items;
}
