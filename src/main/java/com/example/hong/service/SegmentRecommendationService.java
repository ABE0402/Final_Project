// src/main/java/com/example/hong/service/SegmentRecommendationService.java
package com.example.hong.service;

import com.example.hong.domain.AgeBucket;
import com.example.hong.domain.Gender;
import com.example.hong.domain.SegmentType;
import com.example.hong.dto.PlaceCardDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.CafeSegmentScore;
import com.example.hong.entity.User;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.CafeSegmentScoreRepository;
import com.example.hong.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SegmentRecommendationService {

    private final CafeSegmentScoreRepository scoreRepo;
    private final CafeRepository cafeRepo;
    private final ReviewRepository reviewRepository;            // ✅ 추가

    public Map<String, List<PlaceCardDto>> sectionsFor(User user, int topN){
        Map<String, List<PlaceCardDto>> map = new LinkedHashMap<>();
        if (user == null) return map;

        // AGE
        AgeBucket age = AgeBucket.fromBirthDate(user.getBirthDate());
        if (age != null) {
            var ageScores = scoreRepo.findTop50ById_SegmentTypeAndId_SegmentValueOrderByScore30dDesc(
                    SegmentType.AGE, age.code());
            map.put(age.code() + "가 선호하는 카페", toCards(ageScores, topN));
        }
        // GENDER
        if (user.getGender() == Gender.MALE || user.getGender() == Gender.FEMALE) {
            var gScores = scoreRepo.findTop50ById_SegmentTypeAndId_SegmentValueOrderByScore30dDesc(
                    SegmentType.GENDER, user.getGender().name());
            map.put((user.getGender()==Gender.FEMALE?"여성이":"남성이") + " 선호하는 카페", toCards(gScores, topN));
        }
        return map;
    }

    private List<PlaceCardDto> toCards(List<CafeSegmentScore> scores, int topN){
        // 1) 점수에서 cafeId 모으기
        List<Long> ids = scores.stream()
                .map(s -> s.getId().getCafeId())
                .distinct()
                .limit(100)
                .toList();

        if (ids.isEmpty()) return List.of();

        // 2) 일괄 조회: Cafe, 리뷰 개수
        Map<Long, Cafe> cafeMap = cafeRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(Cafe::getId, c -> c));

        Map<Long, Integer> reviewCounts = reviewRepository.countByCafeIds(ids).stream()  // ✅ 여기서 집계
                .collect(Collectors.toMap(
                        ReviewRepository.CafeReviewCount::getCafeId,
                        rc -> Math.toIntExact(rc.getCnt())
                ));

        // 3) DTO 구성
        List<PlaceCardDto> list = new ArrayList<>();
        for (CafeSegmentScore s : scores) {
            Long cafeId = s.getId().getCafeId();
            Cafe c = cafeMap.get(cafeId);
            if (c == null) continue;

            list.add(PlaceCardDto.builder()
                    .type("CAFE")
                    .pathSegment("cafes")
                    .id(c.getId())
                    .name(c.getName())
                    .address(c.getAddressRoad())
                    .heroImageUrl(c.getHeroImageUrl())
                    .averageRating(c.getAverageRating()==null?0.0:c.getAverageRating().doubleValue())
                    .reviewCount(reviewCounts.getOrDefault(cafeId, 0))              // ✅ 수정 포인트
                    .build());

            if (list.size() >= topN) break;
        }
        return list;
    }
}
