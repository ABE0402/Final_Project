package com.example.hong.service;

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

    //사용자의 태그 사용 빈도수 생성
    @Transactional(readOnly = true)
    public Map<String, Long> getUserTagVector(Long userId) {

        List<TagFrequencyDto> frequencies = selectionRepository.findUserTagFrequencies(userId);


        return frequencies.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getTag().getName(),
                        TagFrequencyDto::getFrequency
                ));
    }


    //카페의 태그 set 생성
    @Transactional(readOnly = true)
    public Map<Long, Set<String>> getCafeTagVectors() {
        List<Cafe> allCafesWithTags = cafeRepository.findAllWithTags();

        return allCafesWithTags.stream()
                .collect(Collectors.toMap(
                        Cafe::getId, // Key: 카페의 ID
                        cafe -> cafe.getCafeTags().stream()
                                .map(cafeTag -> cafeTag.getTag().getName())
                                .collect(Collectors.toSet()) // Value: 해당 카페의 태그 이름 Set
                ));
    }


    //사용자에게 유사도 높은 카페 목록 추천
    @Transactional(readOnly = true)
    public List<Long> recommendCafes(Long userId, int topN) {
        // 1단계: 사용자 태그 벡터를 가져옵니다.
        Map<String, Long> userVector = getUserTagVector(userId);
        if (userVector.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Set<String>> cafeVectors = getCafeTagVectors();
        Map<Long, Double> similarities = new HashMap<>();
        for (Map.Entry<Long, Set<String>> entry : cafeVectors.entrySet()) {
            Long cafeId = entry.getKey();
            Set<String> cafeTags = entry.getValue();
            double similarity = calculateCosineSimilarity(userVector, cafeTags);
            similarities.put(cafeId, similarity);
        }

        return similarities.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(topN)
                .collect(Collectors.toList());
    }

    // 사용자, 카페의 벡터 간의 코사인 유사도 계산 헬퍼
    private double calculateCosineSimilarity(Map<String, Long> userVector, Set<String> cafeTags) {
        long dotProduct = 0;
        for (String tag : cafeTags) {
            if (userVector.containsKey(tag)) {
                dotProduct += userVector.get(tag);
            }
        }


        double userVectorMagnitude = 0;
        for (Long frequency : userVector.values()) {
            userVectorMagnitude += Math.pow(frequency, 2);
        }
        userVectorMagnitude = Math.sqrt(userVectorMagnitude);

        double cafeVectorMagnitude = Math.sqrt(cafeTags.size());

        if (userVectorMagnitude == 0 || cafeVectorMagnitude == 0) {
            return 0.0;
        }

        return dotProduct / (userVectorMagnitude * cafeVectorMagnitude);
    }


}