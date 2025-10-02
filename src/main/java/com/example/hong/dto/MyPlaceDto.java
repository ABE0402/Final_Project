package com.example.hong.dto;

import com.example.hong.entity.Cafe;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class MyPlaceDto {

    // ▼▼▼ 모든 필드에서 final 키워드 제거 ▼▼▼
    private String id;
    private String name;
    private Double lat;
    private Double lng;
    private String address;
    private String description;
    private String type;
    private List<String> images;
    private boolean has_parking;
    private boolean has_restroom;
    private boolean has_waiting_room;
    private boolean allows_pets;
    private boolean takeout_available;

    public static MyPlaceDto fromEntity(Cafe cafe) {
        Set<String> tagNames = cafe.getCafeTags().stream()
                .map(cafeTag -> cafeTag.getTag().getName())
                .collect(Collectors.toSet());

        List<String> imageList = Stream.of(
                        cafe.getHeroImageUrl(),
                        cafe.getMenuImageUrl1(),
                        cafe.getMenuImageUrl2(),
                        cafe.getMenuImageUrl3(),
                        cafe.getMenuImageUrl4(),
                        cafe.getMenuImageUrl5()
                )
                .filter(Objects::nonNull)
                .toList();

        MyPlaceDto dto = new MyPlaceDto();
        dto.setId(String.valueOf(cafe.getId()));
        dto.setName(cafe.getName());
        dto.setLat(cafe.getLat().doubleValue());
        dto.setLng(cafe.getLng().doubleValue());
        dto.setAddress(cafe.getAddressRoad());
        dto.setDescription(cafe.getDescription());
        dto.setType("CAFE");
        dto.setImages(imageList);
        dto.setHas_parking(tagNames.contains("주차장"));
        dto.setHas_restroom(tagNames.contains("화장실"));
        dto.setHas_waiting_room(tagNames.contains("대기실"));
        dto.setAllows_pets(tagNames.contains("반려동물 가능"));
        dto.setTakeout_available(tagNames.contains("포장"));

        return dto;
    }
}