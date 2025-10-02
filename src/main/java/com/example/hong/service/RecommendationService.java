package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.dto.TagFrequencyDto;
import com.example.hong.entity.Cafe;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.SearchEventSelectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final SearchEventSelectionRepository selectionRepository;
    private final CafeRepository cafeRepository; // CafeRepository 주입

    /**
     * 특정 사용자의 태그 사용 빈도수(사용자 태그 벡터)를 생성합니다.
     * @param userId 조회할 사용자의 ID
     * @return 태그 이름을 Key, 사용 횟수를 Value로 갖는 Map
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUserTagVector(Long userId) {
        // 1. Repository를 호출하여 DB에서 사용자의 태그 빈도수 데이터를 가져옵니다.
        //    반환 타입을 List<Map<String, Object>>에서 List<TagFrequencyDto>로 수정합니다.
        List<TagFrequencyDto> frequencies = selectionRepository.findUserTagFrequencies(userId);

        // 2. List<TagFrequencyDto>를 Map<String, Long> 형태로 변환합니다.
        //    DTO의 getter를 사용하므로 더 안전하고 깔끔합니다.
        return frequencies.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getTag().getName(), // map.get("tagName") -> dto.getTag().getName()
                        TagFrequencyDto::getFrequency    // map.get("frequency") -> dto.getFrequency()
                ));
    }

    /**
     * 모든 카페 각각의 태그 Set(카페 태그 벡터)을 생성합니다.
     * @return 카페 ID를 Key, 해당 카페의 태그 이름 Set을 Value로 갖는 Map
     */
    @Transactional(readOnly = true)
    public Map<Long, Set<String>> getCafeTagVectors() {
        // 1. Fetch Join 쿼리를 호출하여 모든 카페와 태그 정보를 한 번에 가져옵니다.
        List<Cafe> approvedCafesWithTags =
                cafeRepository.findAllWithTagsByStatus(ApprovalStatus.APPROVED);

        // 2. List<Cafe>를 Map<Long, Set<String>> 형태로 변환합니다.
        // 이것이 바로 '카페별 태그 벡터'입니다.
        return approvedCafesWithTags.stream()
                .collect(Collectors.toMap(
                        Cafe::getId,  // Key: 카페 ID (Long)
                        cafe -> cafe.getCafeTags().stream()
                                .map(ct -> ct.getTag().getName())
                                .collect(Collectors.toCollection(LinkedHashSet::new)) // Value: 태그명 집합
                ));
    }

    /**
     * 특정 사용자에게 가장 유사도가 높은 카페 ID 목록을 추천합니다.
     * @param userId 추천을 받을 사용자의 ID
     * @param topN 추천할 카페의 개수
     * @return 추천 카페 ID의 정렬된 리스트
     */
    @Transactional(readOnly = true)
    public List<Long> recommendCafes(Long userId, int topN) {
        // 1단계: 사용자 태그 벡터를 가져옵니다.
        Map<String, Long> userVector = getUserTagVector(userId);
        if (userVector.isEmpty()) {
            return Collections.emptyList(); // 검색 기록이 없으면 빈 리스트 반환
        }

        // 2단계: 전체 카페 태그 벡터를 가져옵니다.
        Map<Long, Set<String>> cafeVectors = getCafeTagVectors();

        // 3단계: 각 카페와 사용자 간의 코사인 유사도를 계산합니다.
        Map<Long, Double> similarities = new HashMap<>();
        for (Map.Entry<Long, Set<String>> entry : cafeVectors.entrySet()) {
            Long cafeId = entry.getKey();
            Set<String> cafeTags = entry.getValue();
            double similarity = calculateCosineSimilarity(userVector, cafeTags);
            similarities.put(cafeId, similarity);
        }

        // 4단계: 계산된 유사도를 기준으로 카페 ID를 내림차순 정렬합니다.
        return similarities.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // 유사도 높은 순 정렬
                .map(Map.Entry::getKey) // 카페 ID만 추출
                .limit(topN) // 상위 N개만 선택
                .collect(Collectors.toList());
    }

    /**
     * 사용자 벡터와 카페 벡터 간의 코사인 유사도를 계산하는 헬퍼 메서드
     */
    private double calculateCosineSimilarity(Map<String, Long> userVector, Set<String> cafeTags) {
        // 1. 내적(Dot Product) 계산: 사용자가 선호하는 태그 중 카페가 가진 태그의 빈도수 합
        long dotProduct = 0;
        for (String tag : cafeTags) {
            if (userVector.containsKey(tag)) {
                dotProduct += userVector.get(tag);
            }
        }

        // 2. 사용자 벡터 크기(Magnitude) 계산
        double userVectorMagnitude = 0;
        for (Long frequency : userVector.values()) {
            userVectorMagnitude += Math.pow(frequency, 2);
        }
        userVectorMagnitude = Math.sqrt(userVectorMagnitude);

        // 3. 카페 벡터 크기 계산 (카페는 태그 유무만 따지므로, 가진 태그의 개수가 크기)
        double cafeVectorMagnitude = Math.sqrt(cafeTags.size());

        // 분모가 0이 되는 경우 방지
        if (userVectorMagnitude == 0 || cafeVectorMagnitude == 0) {
            return 0.0;
        }

        // 4. 코사인 유사도 계산: (내적) / (벡터A 크기 * 벡터B 크기)
        return dotProduct / (userVectorMagnitude * cafeVectorMagnitude);
    }


}