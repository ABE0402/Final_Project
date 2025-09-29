package com.example.hong.dto;

import com.example.hong.entity.Cafe;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor // Jackson 등 JSON 라이브러리가 객체로 변환할 때 기본 생성자가 필요합니다.
public class CafeSearchResultDto {

    private Long id;
    private String name;
    private String addressRoad;
    private String postcode;
    private String phone;
    private BigDecimal lat;
    private BigDecimal lng;
    private String description;
    private String heroImageUrl;
    private int reviewCount;
    private BigDecimal averageRating;
    private int favoritesCount;
    private String approvalStatus;
    private String operatingHours;
    private String menuText;
    private List<String> menuImageUrls;
    private List<String> tags;
    private List<MenuDto> menus;

    // Entity를 DTO로 변환하는 정적 팩토리 메서드 (로직은 동일)
    public static CafeSearchResultDto fromEntity(Cafe cafe) {
        CafeSearchResultDto dto = new CafeSearchResultDto();

        // 메뉴 목록 DTO로 변환
        List<MenuDto> menuDtos = cafe.getMenus() != null ?
                cafe.getMenus().stream().map(MenuDto::fromEntity).toList() : List.of();

        // 태그 이름 목록 추출
        List<String> tagNames = cafe.getCafeTags() != null ?
                cafe.getCafeTags().stream().map(cafeTag -> cafeTag.getTag().getName()).toList() : List.of();

        // 메뉴 이미지 URL 목록 추출
        List<String> menuImages = Arrays.asList( // List.of -> Arrays.asList 로 변경
                cafe.getMenuImageUrl1(), cafe.getMenuImageUrl2(), cafe.getMenuImageUrl3(),
                cafe.getMenuImageUrl4(), cafe.getMenuImageUrl5()
        ).stream().filter(url -> url != null && !url.isBlank()).toList();

        dto.setId(cafe.getId());
        dto.setName(cafe.getName());
        dto.setAddressRoad(cafe.getAddressRoad());
        dto.setPostcode(cafe.getPostcode());
        dto.setPhone(cafe.getPhone());
        dto.setLat(cafe.getLat());
        dto.setLng(cafe.getLng());
        dto.setDescription(cafe.getDescription());
        dto.setHeroImageUrl(cafe.getHeroImageUrl());
        dto.setReviewCount(cafe.getReviewCount());
        dto.setAverageRating(cafe.getAverageRating());
        dto.setFavoritesCount(cafe.getFavoritesCount());
        dto.setApprovalStatus(cafe.getApprovalStatus() != null ? cafe.getApprovalStatus().name() : null);
        dto.setOperatingHours(cafe.getOperatingHours());
        dto.setMenuText(cafe.getMenuText());
        dto.setMenuImageUrls(menuImages);
        dto.setTags(tagNames);
        dto.setMenus(menuDtos);

        return dto;
    }
}