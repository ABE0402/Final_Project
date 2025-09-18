package com.example.yori.controller;

import com.example.yori.dto.CafeSummaryResponseDto;
import com.example.yori.service.CafeService;
import com.example.yori.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final CafeService cafeService; // 추천된 ID로 실제 카페 정보를 조회하기 위해 주입

    /**
     * API: 현재 로그인한 사용자에게 맞춤 카페 목록을 추천
     * HTTP Method: GET
     * URL: /api/recommend/cafes
     */
    @GetMapping("/cafes")
    public ResponseEntity<List<CafeSummaryResponseDto>> recommendCafes(
            @RequestParam(defaultValue = "10") int topN) { // 추천 개수 (기본 10개)

        // TODO: 로그인 기능 구현 후 실제 사용자 ID를 가져와야 합니다. (현재는 임시 ID 1 사용)
        Long currentUserId = 1L;

        // 1. RecommendationService를 호출하여 추천 카페 ID 목록을 받습니다.
        List<Long> recommendedCafeIds = recommendationService.recommendCafes(currentUserId, topN);

        if (recommendedCafeIds.isEmpty()) {
            return ResponseEntity.ok(List.of()); // 추천 결과가 없으면 빈 리스트 반환
        }

        // 2. ID 목록을 이용해 실제 카페 정보를 DTO 리스트로 조회합니다.
        List<CafeSummaryResponseDto> recommendedCafes = recommendedCafeIds.stream()
                .map(cafeId -> cafeService.getCafeSummaryById(cafeId)) // ID로 간략 정보 조회
                .collect(Collectors.toList());

        return ResponseEntity.ok(recommendedCafes);
    }
}