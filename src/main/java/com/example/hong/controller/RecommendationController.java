package com.example.hong.controller;

import com.example.hong.dto.CafeSummaryResponseDto;
import com.example.hong.service.CafeService;
import com.example.hong.service.RecommendationService;
import com.example.hong.service.auth.AppUserPrincipal; // Principal 클래스 import
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // HttpStatus import
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 어노테이션 import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final CafeService cafeService;

    @GetMapping("/cafes")
    public ResponseEntity<List<CafeSummaryResponseDto>> recommendCafes(
            @RequestParam(defaultValue = "10") int topN,
            @AuthenticationPrincipal AppUserPrincipal userPrincipal // [수정됨] 로그인 정보 받아오기
    ) {
        // [수정됨] 비로그인 사용자 접근 제어
        if (userPrincipal == null) {
            // 로그인하지 않은 사용자에게는 권한 없음(401) 또는 빈 리스트를 반환할 수 있습니다.
            // 개인화 추천이므로 권한 없음을 알리는 것이 더 명확합니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // [수정됨] 임시 ID 대신 실제 로그인한 사용자의 ID를 사용
        Long currentUserId = userPrincipal.getId();

        // --- (이하 로직은 동일) ---
        List<Long> recommendedCafeIds = recommendationService.recommendCafes(currentUserId, topN);

        if (recommendedCafeIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<CafeSummaryResponseDto> recommendedCafes = recommendedCafeIds.stream()
                .map(cafeId -> cafeService.getCafeSummaryById(cafeId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(recommendedCafes);
    }
}