package com.example.hong.dto;

import com.example.hong.domain.StoreType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerApplyRequestDto {
    private StoreType storeType;     // CAFE / RESTAURANT
    private String storeName;
    private String businessNumber;
    private String ownerRealName;
    private String contactPhone;
    private String storeAddress;
}