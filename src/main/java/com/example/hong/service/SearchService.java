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

    // 생성자에서 연관 태그 파일을 로드
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

    @Transactional
    public List<CafeSearchResultDto> searchCafesAndLog(SearchRequestDto request, Long userId) {
        // 1. 검색 이벤트(SearchEvent) 로그를 먼저 저장하고, 저장된 객체를 받아옵니다.
        SearchEvent event = logSearchEvent(request, userId);

        List<String> originalTags = extractAllTagNames(request);
        List<String> expandedTags = expandTags(originalTags); // 연관 태그 추가
        // 2. event가 null이 아니고(즉, 로깅이 필요한 검색이었고), 사용자가 선택한 필터(태그)가 있다면 로그를 저장합니다.
        if (event != null) {
            List<String> selectedTagNames = extractAllTagNames(request);

            // [디버깅 로그 추가 1] DTO에서 태그가 제대로 추출되었는지 확인
            log.info("### DTO에서 추출된 태그 이름: {}", selectedTagNames);


            if (!selectedTagNames.isEmpty()) {
                logTagSelections(event, selectedTagNames);
            }
        }

        // 3. Querydsl을 이용해 모든 조건(키워드+필터)에 맞는 카페 목록을 검색합니다.
        List<Cafe> cafes = cafeRepository.search(request);

        // 4. DTO로 변환하여 반환합니다.
        return cafes.stream()
                .map(CafeSearchResultDto::fromEntity)
                .collect(Collectors.toList());
    }


    // [추가됨] 태그 목록을 확장하는 헬퍼 메소드
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

    /**
     * [수정됨] SearchEvent 로그를 생성하고, 저장된 객체를 반환합니다.
     */
    private SearchEvent logSearchEvent(SearchRequestDto request, Long userId) {
        boolean hasTextQuery = StringUtils.hasText(request.getQuery());
        if (!hasTextQuery && extractAllTagNames(request).isEmpty()) {
            return null; // 검색 조건이 아무것도 없으면 로깅하지 않고 null 반환
        }

        SearchEvent.SearchType type = hasTextQuery ? SearchEvent.SearchType.TEXT : SearchEvent.SearchType.CATEGORY;

        SearchEvent event = SearchEvent.builder()
                .userId(userId)
                .searchType(type)
                .searchQuery(request.getQuery())
                .build();

        // 저장된 엔티티를 반환하여 FK로 사용할 수 있도록 함
        return searchEventRepository.save(event);
    }

    /**
     * 선택된 태그들의 로그(SearchEventSelection)를 저장합니다.
     */
    private void logTagSelections(SearchEvent event, List<String> tagNames) {
        // [디버깅 로그 추가 2] 이 메소드가 실제로 호출되었는지 확인
        log.info("### logTagSelections 메소드 실행됨. 전달받은 태그: {}", tagNames);

        List<Tag> tags = tagRepository.findByNameIn(tagNames);

        // [디버깅 로그 추가 3] DB에서 태그를 제대로 찾아왔는지 확인 (가장 중요!)
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

    /**
     * [수정됨] SearchRequestDto의 모든 태그 필드를 하나의 리스트로 안전하게 합칩니다.
     */
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
//        // SearchEventRepository에 findTop5SearchQueries() 메소드를 추가해야 합니다.
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