// src/main/java/.../config/TagDataInitializer.java
package com.example.hong.config;

import com.example.hong.entity.Tag;
import com.example.hong.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TagDataInitializer implements CommandLineRunner {

    private final TagRepository tagRepository;

    @Override
    public void run(String... args) {
        // (name, category)
        List<TagSeed> seeds = List.of(
                // 공통: 동반인
                s("1인","companion"), s("친구","companion"), s("커플","companion"), s("가족","companion"), s("단체","companion"),
                // 카페 분위기
                s("조용한","mood"), s("대화하기 좋은","mood"), s("신나는","mood"),
                s("카공하기 좋은","mood"), s("분위기 좋은","mood"), s("데이트하기 좋은","mood"),
                // 음식점 분위기 추가
                s("혼밥하기 좋은","mood"), s("사진 맛집","mood"),
                // 편의/서비스
                s("주차장","amenities"), s("화장실","amenities"), s("대기실","amenities"), s("반려동물 가능","amenities"), s("포장","amenities"),
                // 예약여부
                s("가능","reservation"), s("불가능","reservation"),
                // 우선순위
                s("많이 찾는","priority"), s("리뷰 많은","priority"), s("평점 높은","priority"), s("즐겨찾기 많은","priority"),
                // 카페 종류
                s("디저트 전문","type"), s("커피 전문","type"), s("인테리어 맛집","type"),
                // 음식점 종류
                s("한식","type"), s("중식","type"), s("일식","type"),
                s("양식","type"), s("퓨전","type"), s("아시안","type")
        );

        for (TagSeed s : seeds) {
            tagRepository.findByCategoryAndName(s.category(), s.name())
                    .orElseGet(() -> tagRepository.save(Tag.of(s.name(), s.category()))); // ✅ 여기!
        }
    }

    private static TagSeed s(String name, String category) {
        return new TagSeed(name, category);
    }
    private record TagSeed(String name, String category) {}

}
