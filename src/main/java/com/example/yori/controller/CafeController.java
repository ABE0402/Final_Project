package com.example.yori.controller;

import com.example.yori.service.CafeService;
import com.example.yori.dto.CafeCreateRequestDto;
import com.example.yori.dto.CafeDetailResponseDto;
import com.example.yori.dto.CafeSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController // REST API를 위한 컨트롤러
@RequestMapping("/api/cafes") // 이 컨트롤러의 모든 경로는 /api/cafes로 시작
@RequiredArgsConstructor
public class CafeController {

    private final CafeService cafeService;

    /**
     * API: 새로운 카페 등록
     * HTTP Method: POST
     * URL: /api/cafes
     */
    @PostMapping
    public ResponseEntity<Void> createCafe(@RequestBody CafeCreateRequestDto requestDto) {
        // TODO: 현재 로그인한 사용자의 ID를 가져와야 합니다. (지금은 임시로 1L 사용)
        Long currentUserId = 1L;

        Long cafeId = cafeService.createCafe(requestDto, currentUserId);

        // 생성된 카페의 URI를 Location 헤더에 담아 201 Created 응답을 보냅니다.
        return ResponseEntity.created(URI.create("/api/cafes/" + cafeId)).build();
    }

    /**
     * API: 특정 카페 상세 정보 조회
     * HTTP Method: GET
     * URL: /api/cafes/{cafeId}
     */
    @GetMapping("/{cafeId}")
    public ResponseEntity<CafeDetailResponseDto> getCafeById(@PathVariable Long cafeId) {
        CafeDetailResponseDto cafeInfo = cafeService.getCafeById(cafeId);
        return ResponseEntity.ok(cafeInfo); // 200 OK와 함께 카페 상세 정보를 응답 바디에 담아 반환
    }

    /**
     * API: 전체 카페 목록 조회
     * HTTP Method: GET
     * URL: /api/cafes
     */
    @GetMapping
    public ResponseEntity<List<CafeSummaryResponseDto>> getAllCafes() {
        List<CafeSummaryResponseDto> cafes = cafeService.getAllCafes();
        return ResponseEntity.ok(cafes); // 200 OK와 함께 카페 목록을 응답 바디에 담아 반환
    }
}