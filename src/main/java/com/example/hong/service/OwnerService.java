package com.example.hong.service;

import com.example.hong.dto.ShopCreateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OwnerService {

    public List<Map<String,Object>> myShops(Authentication auth) {
        // TODO: 실제 Cafe/Restaurant를 소유자 기준으로 조회
        return List.of(); // 임시
    }

    public void submitShop(Authentication auth, ShopCreateRequestDto req) {
        // TODO: 실제 저장 (승인 대기 엔티티/테이블 or draft 테이블)
        // - type에 따라 카페/맛집 대기 테이블에 넣고 is_visible=false
        // - 대표이미지 저장 후 URL 보관
    }
}