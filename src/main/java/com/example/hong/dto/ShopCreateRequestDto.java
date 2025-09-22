package com.example.hong.dto;

import lombok.Data;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Data
@Setter
public class ShopCreateRequestDto {
    private String type;        // "CAFE" or "RESTAURANT"
    private String name;
    private String phone;

    private String postcode;
    private String addressRoad;     // 도로명 주소 (주입 필수)
    private String addressDetail;   // 상세 주소 (선택)

    private Double lat;             // 선택(클라이언트가 채우면 백엔드 검증만)
    private Double lng;             // 선택      // -> Cafe.lng(BigDecimal)
    private String description;
    private MultipartFile image;
}