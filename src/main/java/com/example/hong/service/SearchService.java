package com.example.hong.service;

import com.example.hong.dto.CafeSummaryResponseDto;
import com.example.hong.dto.SearchRequestDto;
import com.example.hong.entity.SearchEvent;
import com.example.hong.entity.SearchEventSelection;
import com.example.hong.entity.Tag;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.SearchEventRepository;
import com.example.hong.repository.SearchEventSelectionRepository;
import com.example.hong.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    @Transactional
    public List<CafeSummaryResponseDto> searchCafesAndLog(SearchRequestDto request, Long userId) {

        // 1. 검색 이벤트(SearchEvent) 로그를 먼저 저장합니다.
        SearchEvent event = logSearchEvent(request, userId);

        // 2. 사용자가 선택한 필터(태그)가 있다면, 해당 태그들을 조회하고 로그(SearchEventSelection)를 저장합니다.
        List<String> selectedTagNames = extractAllTagNames(request);
        if (!selectedTagNames.isEmpty()) {
            logTagSelections(event, selectedTagNames);
        }

        // 3. Querydsl을 이용해 조건에 맞는 카페 목록을 검색합니다.
        return cafeRepository.search(request).stream()
                .map(CafeSummaryResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * SearchEvent 로그를 생성하고 저장합니다.
     */
    private SearchEvent logSearchEvent(SearchRequestDto request, Long userId) {
        boolean hasTextQuery = StringUtils.hasText(request.getQuery());
        SearchEvent.SearchType type = hasTextQuery ? SearchEvent.SearchType.TEXT : SearchEvent.SearchType.CATEGORY;

        SearchEvent event = SearchEvent.builder()
                // 수정된 부분: intValue()를 삭제하고 userId를 그대로 전달
                .userId(userId)
                .searchType(type)
                .searchQuery(request.getQuery())
                .build();
        return searchEventRepository.save(event);
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
}