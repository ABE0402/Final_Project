package com.example.hong.service;

import com.example.hong.dto.CafeSearchResultDto;
import com.example.hong.dto.CafeSummaryResponseDto;
import com.example.hong.dto.MenuDto;
import com.example.hong.dto.SearchRequestDto;
import com.example.hong.entity.*;
import com.example.hong.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final CafeRepository cafeRepository;
    private final SearchEventRepository searchEventRepository;
    private final TagRepository tagRepository;
    private final SearchEventSelectionRepository searchEventSelectionRepository;
    private final SearchLogRepository searchLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<CafeSearchResultDto> searchCafesAndLog(SearchRequestDto request, Long userId) {
        // 1. 검색 이벤트(SearchEvent) 로그 저장 (텍스트/카테고리 구분 포함)
        logSearchEvent(request, userId);

        // 2. 선택된 태그가 있다면 로그 저장
        List<String> selectedTagNames = extractAllTagNames(request);
        if (!selectedTagNames.isEmpty()) {
            //logTagSelections(event, selectedTagNames); // logSearchEvent와 통합 또는 별도 로깅
        }

        // 3. Querydsl을 이용해 모든 조건(키워드+필터)에 맞는 카페 목록 검색
        List<Cafe> cafes = cafeRepository.search(request);

        // 4. DTO로 변환하여 반환
        return cafes.stream()
                .map(CafeSearchResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * SearchEvent 로그를 생성하고 저장합니다.
     */
    private void logSearchEvent(SearchRequestDto request, Long userId) {
        boolean hasTextQuery = StringUtils.hasText(request.getQuery());
        // 텍스트, 필터 조건 모두 없으면 로깅하지 않음 (선택 사항)
        if (!hasTextQuery && extractAllTagNames(request).isEmpty()) {
            return;
        }

        SearchEvent.SearchType type = hasTextQuery ? SearchEvent.SearchType.TEXT : SearchEvent.SearchType.CATEGORY;

        // User 엔티티를 직접 참조하도록 SearchEvent를 수정했다면 아래 로직이 더 좋습니다.
        // User user = (userId != null) ? userRepository.findById(userId).orElse(null) : null;
        SearchEvent event = SearchEvent.builder()
                .userId(userId) // 또는 .user(user)
                .searchType(type)
                .searchQuery(request.getQuery())
                .build();
        searchEventRepository.save(event);
    }

    /**
     * 선택된 태그들의 로그(SearchEventSelection)를 저장합니다.
     */
    private void logTagSelections(SearchEvent event, List<String> tagNames) {
        // DB에서 태그 이름으로 실제 Tag 엔티티들을 한 번에 조회합니다.
        List<Tag> tags = tagRepository.findByNameIn(tagNames);

        // 각 Tag에 대해 SearchEventSelection 객체를 생성합니다.
        List<SearchEventSelection> selections = tags.stream()
                .map(tag -> new SearchEventSelection(event, tag))
                .collect(Collectors.toList());

        // 생성된 로그들을 DB에 한 번에 저장합니다.
        searchEventSelectionRepository.saveAll(selections);
    }

    /**
     * SearchRequestDto에 담긴 모든 태그 관련 필터 값들을 하나의 리스트로 합칩니다.
     */

    private List<String> extractAllTagNames(SearchRequestDto request) {
        List<String> tagNames = new ArrayList<>();
        if (StringUtils.hasText(request.getCompanion())) tagNames.addAll(List.of(request.getCompanion().split(",")));
        if (StringUtils.hasText(request.getMood())) tagNames.addAll(List.of(request.getMood().split(",")));
        if (StringUtils.hasText(request.getAmenities())) tagNames.addAll(List.of(request.getAmenities().split(",")));
        if (StringUtils.hasText(request.getDays())) tagNames.addAll(List.of(request.getDays().split(",")));
        if (StringUtils.hasText(request.getType())) tagNames.addAll(List.of(request.getType().split(",")));

        return tagNames;
    }


    // 상위 5개 인기 검색어를 조회
    public List<String> getTopKeywords() {
        return searchLogRepository.findTop5Keywords().stream()
                .map(r -> (String) r[0])
                .toList();
    }

    // 최근 30일 사용자 맞춤 추천
    public List<String> getUserTopKeywords(Long userId) {
        return searchLogRepository.findUserKeywordCountLast30Days(userId).stream()
                .map(r -> (String) r[0])
                .toList();
    }

    // 인기 검색어
    public List<String> getTopKeywordsWeekly() {
        int year = LocalDateTime.now().getYear();
        int weekMonth = LocalDateTime.now().getMonthValue(); // 주는 단순화해서 월 기준
        return searchLogRepository.findTopKeywords(year, weekMonth).stream()
                .limit(5)
                .map(r -> (String) r[0])
                .toList();
    }

    public List<String> getTopKeywordsMonthly() {
        int year = LocalDateTime.now().getYear();
        int month = LocalDateTime.now().getMonthValue();
        return searchLogRepository.findTopKeywords(year, month).stream()
                .limit(5)
                .map(r -> (String) r[0])
                .toList();
    }

    public List<String> getTopKeywordsYearly() {
        int year = LocalDateTime.now().getYear();
        return searchLogRepository.findTopKeywords(year, null).stream()
                .limit(5)
                .map(r -> (String) r[0])
                .toList();
    }
}