package com.example.hong.service;

import com.example.hong.entity.Cafe;
import com.example.hong.entity.User;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.UserRepository;
import com.example.hong.dto.CafeCreateRequestDto;
import com.example.hong.dto.CafeDetailResponseDto;
import com.example.hong.dto.CafeSummaryResponseDto;
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

    //새로운 카페 등록
    @Transactional
    public Long createCafe(CafeCreateRequestDto requestDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. id=" + ownerId));
        Cafe cafe = requestDto.toEntity(owner);
        return cafeRepository.save(cafe).getId();
    }

   //특정 카페 조회
    @Transactional(readOnly = true)
    public CafeDetailResponseDto getCafeById(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카페를 찾을 수 없습니다. id=" + cafeId));
        return CafeDetailResponseDto.from(cafe);
    }

    //전체 카페 조회
    @Transactional(readOnly = true)
    public List<CafeSummaryResponseDto> getAllCafes() {
        return cafeRepository.findAll().stream()
                .map(CafeSummaryResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CafeSummaryResponseDto getCafeSummaryById(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카페를 찾을 수 없습니다. id=" + cafeId));

        return CafeSummaryResponseDto.from(cafe);
    }
}