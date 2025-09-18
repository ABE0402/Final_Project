package com.example.yori.service;

import com.example.yori.entity.Cafe;
import com.example.yori.entity.User;
import com.example.yori.repository.CafeRepository;
import com.example.yori.repository.UserRepository; // UserRepository가 필요합니다.
import com.example.yori.dto.CafeCreateRequestDto;
import com.example.yori.dto.CafeDetailResponseDto;
import com.example.yori.dto.CafeSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class CafeService {

    private final CafeRepository cafeRepository;
    private final UserRepository userRepository; // User를 조회하기 위해 주입

    /**
     * 새로운 카페를 등록하는 메서드
     */
    @Transactional
    public Long createCafe(CafeCreateRequestDto requestDto, Long ownerId) {
        // 1. DTO로부터 가게 주인의 User 엔티티를 조회합니다.
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. id=" + ownerId));

        // 2. DTO를 Cafe 엔티티로 변환합니다. (owner 정보 포함)
        Cafe cafe = requestDto.toEntity(owner);

        // 3. Cafe 엔티티를 DB에 저장하고 생성된 ID를 반환합니다.
        return cafeRepository.save(cafe).getId();
    }

    /**
     * 특정 카페의 상세 정보를 조회하는 메서드
     */
    @Transactional(readOnly = true)
    public CafeDetailResponseDto getCafeById(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카페를 찾을 수 없습니다. id=" + cafeId));

        // 조회한 Cafe 엔티티를 CafeDetailResponseDto로 변환하여 반환합니다.
        return CafeDetailResponseDto.from(cafe);
    }

    /**
     * 전체 카페 목록을 간략하게 조회하는 메서드
     */
    @Transactional(readOnly = true)
    public List<CafeSummaryResponseDto> getAllCafes() {
        return cafeRepository.findAll().stream()
                .map(CafeSummaryResponseDto::from) // 각 Cafe 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CafeSummaryResponseDto getCafeSummaryById(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카페를 찾을 수 없습니다. id=" + cafeId));

        return CafeSummaryResponseDto.from(cafe);
    }
}