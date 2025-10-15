package com.example.hong.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.geo.GeoPoint; // GeoPoint import

@Getter
@Builder
@Document(indexName = "places") // 인덱스 이름을 "places"로 변경
public class PlaceDocument {
    @Id
    private Long id; // CSV의 '상가업소번호'와 동일하게 사용

    @Field(type = FieldType.Text, analyzer = "nori")
    private String name; // 상호명

    @Field(type = FieldType.Text, analyzer = "nori")
    private String address; // 도로명 주소

    // [핵심] "카페", "한식" 등 업종을 저장할 필드
    @Field(type = FieldType.Keyword)
    private String category;

    // [추천] 위도/경도를 위한 필드
    @Field(type = FieldType.Object)
    private GeoPoint location; // 위도, 경도를 GeoPoint 객체로 저장

}