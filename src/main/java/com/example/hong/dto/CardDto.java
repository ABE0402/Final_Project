package com.example.hong.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {
    private Long id;
    private String type;
    private String pathSegment;
    private String name;
    private String address;
    private String heroImageUrl;
    private Double averageRating;
    private Integer reviewCount;

    @Builder.Default
    private String hashtags = "";
}
