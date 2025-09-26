package com.example.hong.repository;

import com.example.hong.entity.SearchLog;
import com.example.hong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
    Optional<SearchLog> findByUserAndKeyword(User user, String keyword);

    //상위 5개 인기 검색어를 조회
    @Query(value = """
        SELECT sl.keyword, SUM(sl.search_count) AS total
        FROM search_log sl
        GROUP BY sl.keyword
        ORDER BY total DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> findTop5Keywords();

    // 30일 기준 사용자 검색어 집계
    @Query("""
        SELECT sl.keyword, COUNT(sl) as cnt
        FROM SearchLog sl
        WHERE sl.user.id = :userId
          AND sl.lastSearched >= CURRENT_DATE - 30
        GROUP BY sl.keyword
        ORDER BY cnt DESC
    """)
    List<Object[]> findUserKeywordCountLast30Days(@Param("userId") Long userId);

    // 상위 N개 인기 검색어 (주간, 월간, 연간)
    @Query("""
        SELECT sl.keyword, SUM(sl.searchCount) as totalCount
        FROM SearchLog sl
        WHERE YEAR(sl.lastSearched) = :year
          AND (:month IS NULL OR MONTH(sl.lastSearched) = :month)
        GROUP BY sl.keyword
        ORDER BY totalCount DESC
    """)
    List<Object[]> findTopKeywords(@Param("year") int year, @Param("month") Integer month);
}
