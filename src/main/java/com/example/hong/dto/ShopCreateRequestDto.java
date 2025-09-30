package com.example.hong.dto;

import lombok.Data;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Setter
public class ShopCreateRequestDto {
    private String type;        // "CAFE" or "RESTAURANT"
    private String name;
    private String phone;           // 가게 전화번호
    private String businessNumber;  // 사업자 번호
    private String postcode;        // 우편번호
    //사업자 번호
    private String addressRoad;     // 도로명 주소 (주입 필수)

    private Double lat;             // 선택(클라이언트가 채우면 백엔드 검증만)
    private Double lng;             // 선택      // -> Cafe.lng(BigDecimal)
    private String description;
    private MultipartFile image;

    private String operatingHours;       // 텍스트
    private String menuText;             // 텍스트
    private List<MultipartFile> menuImages;

    private List<Integer> tagIds;

}