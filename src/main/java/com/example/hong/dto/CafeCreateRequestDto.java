package com.example.hong.dto;

import com.example.hong.entity.Cafe;
import com.example.hong.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CafeCreateRequestDto {

    private String name;
    private String phone;
    private String addressRoad;
    private String postcode;
    private String description;


    private Double lat;   // 위도
    private Double lng;   // 경도

    // DTO -> Entity
    public Cafe toEntity(User owner) {
        Cafe.CafeBuilder builder = Cafe.builder()
                .name(this.name)
                .phone(this.phone)
                .addressRoad(this.addressRoad)
                .postcode(this.postcode)
                .description(this.description)
                .owner(owner);


        if (this.lat != null) builder.lat(BigDecimal.valueOf(this.lat));
        if (this.lng != null) builder.lng(BigDecimal.valueOf(this.lng));

        return builder.build();
    }
}