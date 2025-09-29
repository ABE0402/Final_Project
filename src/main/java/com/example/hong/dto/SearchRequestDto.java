package com.example.hong.dto;

import lombok.Data;

@Data // @Getter, @Setter, @ToString 등 Lombok의 유용한 기능을 합친 어노테이션
public class SearchRequestDto {

    // from <input name="query">
    private String query;

    // from <input name="category">
    private String category;

    // from <input name="companion">
    private String companion;

    // from <input name="mood">
    private String mood;

    // from <input name="amenities">
    private String amenities;

    // from <input name="days">
    private String days;

    // from <input name="sort">
    private String sort;

    // from <input name="type">
    private String type;

    private String reservation;
}