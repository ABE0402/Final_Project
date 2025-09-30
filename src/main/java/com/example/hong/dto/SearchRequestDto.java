package com.example.hong.dto;

import lombok.Data;

@Data
public class SearchRequestDto {
    private String query;
    private String companion;
    private String mood;
    private String amenities;
    private String reservation;
    private String type;
    // 'priority'는 삭제하고 'sort'로 통일
    private String sort;
}
