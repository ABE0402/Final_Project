package com.example.hong.service;

import com.example.hong.dto.CafeDto;
import com.example.hong.dto.CafeResultSection;
import com.example.hong.dto.MenuDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.SearchLog;
import com.example.hong.entity.User;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.SearchLogRepository;
import com.example.hong.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final CafeRepository cafeRepository;
    private final SearchLogRepository searchLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<CafeDto> searchCafes(String keyword, Long UserId) {
        // 1. 사용자 확인
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("없는 사용자"));
        // 임시 유저
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID: 1"));

        // 1. 검색어 저장
        saveSearchLog(user, keyword);

        // 2. 카페 검색
        List<Cafe> cafes = cafeRepository.searchByKeyword(keyword);

        // 3. 카페별 섹션 생성
        return cafes.stream().map(c -> {
            // 메뉴 필터링
            List<MenuDto> filteredMenus = c.getMenus().stream()
                    .filter(m -> m.getName().toLowerCase().contains(keyword.toLowerCase()))
                    .map(MenuDto::fromEntity)
                    .toList();

            CafeDto dto = CafeDto.fromEntity(c);
            dto.setMenus(filteredMenus);
            return dto;
        }).toList();
    }

    public void saveSearchLog(User user, String keyword) {
        searchLogRepository.findByUserAndKeyword(user, keyword)
                .ifPresentOrElse(
                        log -> {
                            log.setSearchCount(log.getSearchCount() + 1);
                            log.setLastSearched(LocalDateTime.now());
                            searchLogRepository.save(log);
                        },
                        () -> {
                            SearchLog log = new SearchLog();
                            log.setUser(user);
                            log.setKeyword(keyword);
                            log.setSearchCount(1);
                            log.setLastSearched(LocalDateTime.now());
                            searchLogRepository.save(log);
                        }
                );
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
