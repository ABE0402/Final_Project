package com.example.yori.dto;

import com.example.yori.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 쿼리 결과를 바로 매핑하기 위해 모든 필드를 갖는 생성자가 필요합니다.
public class TagFrequencyDto {
    private Tag tag;
    private long frequency;
}