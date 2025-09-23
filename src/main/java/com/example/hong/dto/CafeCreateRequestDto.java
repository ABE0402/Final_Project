package com.example.hong.dto;

import com.example.hong.entity.Cafe;
import com.example.hong.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CafeCreateRequestDto {

    private String name;
    private String phone;
    private String addressRoad;
    private String postcode;
    private String description;

    // DTO를 Entity로 변환하는 메서드
    public Cafe toEntity(User owner) {
        return Cafe.builder()
                .name(this.name)
                .phone(this.phone)
                .addressRoad(this.addressRoad)
                .postcode(this.postcode)
                .description(this.description)
                .owner(owner) // 가게 주인(User) 정보 설정
                .build();
    }
}