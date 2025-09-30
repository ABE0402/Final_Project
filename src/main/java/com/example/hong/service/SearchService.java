package com.example.hong.service;

import com.example.hong.dto.CafeSearchResultDto;
import com.example.hong.dto.SearchRequestDto;
import com.example.hong.entity.*;
import com.example.hong.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    private final CafeRepository cafeRepository;
    private final SearchEventRepository searchEventRepository;
    private final TagRepository tagRepository;
    private final SearchEventSelectionRepository searchEventSelectionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, List<String>> relatedTagsMap;


    @PostConstruct
    public void init() {
        try {
            // 파일을 읽어서 Map으로 변환
            InputStream is = new ClassPathResource("related_tags.json").getInputStream();
            this.relatedTagsMap = objectMapper.readValue(is, new TypeReference<>() {});
        } catch (IOException e) {
            this.relatedTagsMap = new HashMap<>();
            log.error("related_tags.json 파일을 로드하는데 실패했습니다.", e);
        }
    }


    //검색
    @Transactional
    public List<CafeSearchResultDto> searchCafesAndLog(SearchRequestDto request, Long userId) {

        SearchEvent event = logSearchEvent(request, userId);
        List<String> originalTags = extractAllTagNames(request);
        if (event != null && !originalTags.isEmpty()) {
            logTagSelections(event, originalTags);
        }
        List<String> expandedTags = expandTags(originalTags);


        List<Cafe> candidates = cafeRepository.search(request, expandedTags);

        String query = StringUtils.hasText(request.getQuery()) ? request.getQuery().trim() : "";
        log.info("searchCafesAndLog: candidates={}, originalTags={}, expandedTags={}, query='{}'",
                candidates.size(), originalTags, expandedTags, query);

        boolean hasTags = !originalTags.isEmpty();
        Set<String> userTagSet = new HashSet<>(originalTags);

        return candidates.stream()
                .map(cafe -> {
                    double similarity;
                    if (hasTags) {
                        Set<String> cafeTags = cafe.getCafeTags().stream()
                                .map(ct -> ct.getTag().getName())
                                .collect(Collectors.toSet());
                        similarity = jaccard(userTagSet, cafeTags);
                    } else if (StringUtils.hasText(query)) {
                        String q = query.toLowerCase();
                        String name = (cafe.getName() == null ? "" : cafe.getName().toLowerCase());
                        String desc = (cafe.getDescription() == null ? "" : cafe.getDescription().toLowerCase());
                        String addr = (cafe.getAddressRoad() == null ? "" : cafe.getAddressRoad().toLowerCase());

                        if (name.contains(q)) similarity = 1.0;
                        else if (desc.contains(q) || addr.contains(q)) similarity = 0.5;
                        else similarity = 0.0;
                    } else {
                        similarity = 0.0;
                    }

                    CafeSearchResultDto dto = CafeSearchResultDto.fromEntity(cafe);
                    dto.setSimilarity(similarity);
                    return dto;
                })

                // 필터는 '태그 검색'을 한 경우에만 유사도 기준으로 적용
                .filter(dto -> {
                    if (hasTags) {
                        return dto.getSimilarity() >= 0.1;
                    } else {
                        return true;
                    }
                })
                .sorted(Comparator.comparing(CafeSearchResultDto::getSimilarity).reversed())
                .collect(Collectors.toList());
    }

    // 자카드 유사도 계산 헬퍼 메소드
    private double jaccard(Set<String> s1, Set<String> s2) {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        return (double) intersection.size() / union.size();
    }



    //  태그 목록을 확장하는 헬퍼 메소드
    private List<String> expandTags(List<String> originalTags) {
        if (relatedTagsMap.isEmpty()) {
            return originalTags;
        }

        Set<String> expanded = new HashSet<>(originalTags);
        originalTags.forEach(tag -> {
            if (relatedTagsMap.containsKey(tag)) {
                expanded.addAll(relatedTagsMap.get(tag));
            }
        });
        return new ArrayList<>(expanded);
    }


    // SearchEvent 로그를 생성하고, 저장된 객체를 반환합니다.

    private SearchEvent logSearchEvent(SearchRequestDto request, Long userId) {
        boolean hasTextQuery = StringUtils.hasText(request.getQuery());
        if (!hasTextQuery && extractAllTagNames(request).isEmpty()) {
            return null;
        }

        SearchEvent.SearchType type = hasTextQuery ? SearchEvent.SearchType.TEXT : SearchEvent.SearchType.CATEGORY;

        SearchEvent event = SearchEvent.builder()
                .userId(userId)
                .searchType(type)
                .searchQuery(request.getQuery())
                .build();


        return searchEventRepository.save(event);
    }


    private void logTagSelections(SearchEvent event, List<String> tagNames) {

        log.info("### logTagSelections 메소드 실행됨. 전달받은 태그: {}", tagNames);

        List<Tag> tags = tagRepository.findByNameIn(tagNames);


        log.info("### DB에서 조회된 Tag 엔티티 개수: {}개", tags.size());
        if (!tags.isEmpty()) {
            log.info("### 조회된 태그 ID: {}", tags.stream().map(Tag::getId).toList());
        }

        List<SearchEventSelection> selections = tags.stream()
                .map(tag -> new SearchEventSelection(event, tag))
                .collect(Collectors.toList());
        if (!selections.isEmpty()) {
            searchEventSelectionRepository.saveAll(selections);
        }
    }

    private List<String> extractAllTagNames(SearchRequestDto request) {
        return Stream.of(
                        request.getCompanion(),
                        request.getMood(),
                        request.getAmenities(),
                        request.getReservation(),
                        request.getType()
                        // request.getDays() 필드가 DTO에 있다면 추가
                )
                .filter(StringUtils::hasText)
                .flatMap(tags -> Arrays.stream(tags.split(",")))
                .map(String::trim) // 쉼표 기준 분리 후 각 태그의 앞뒤 공백 제거
                .collect(Collectors.toList());
    }

//    /**
//     *  SearchEvent 테이블을 사용하여 인기 검색어를 조회합니다. 나중에 구현할거
//     */
//    public List<String> getTopKeywords() {
//        // SearchEventRepository에 findTop5SearchQueries() 메소드를 추가
//        return searchEventRepository.findTop5SearchQueries();
//    }
//
//    // 인기 검색어
//    public List<String> getTopKeywordsWeekly() {
//        int year = LocalDateTime.now().getYear();
//        int weekMonth = LocalDateTime.now().getMonthValue(); // 주는 단순화해서 월 기준
//        return searchLogRepository.findTopKeywords(year, weekMonth).stream()
//                .limit(5)
//                .map(r -> (String) r[0])
//                .toList();
//    }
//
//    public List<String> getTopKeywordsMonthly() {
//        int year = LocalDateTime.now().getYear();
//        int month = LocalDateTime.now().getMonthValue();
//        return searchLogRepository.findTopKeywords(year, month).stream()
//                .limit(5)
//                .map(r -> (String) r[0])
//                .toList();
//    }
//
//    public List<String> getTopKeywordsYearly() {
//        int year = LocalDateTime.now().getYear();
//        return searchLogRepository.findTopKeywords(year, null).stream()
//                .limit(5)
//                .map(r -> (String) r[0])
//                .toList();
//    }
}