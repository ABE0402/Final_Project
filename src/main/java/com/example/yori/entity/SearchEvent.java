package com.example.yori.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Search_Events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_event_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId; // 사용자 ID

    @CreationTimestamp
    @Column(name = "searched_at", updatable = false)
    private LocalDateTime searchedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "search_type", nullable = false)
    private SearchType searchType;

    @Column(name = "search_query")
    private String searchQuery;

    @Builder
    // ============== 수정된 부분 =================
    public SearchEvent(Long userId, SearchType searchType, String searchQuery) {
        this.userId = userId;
        this.searchType = searchType;
        this.searchQuery = searchQuery;
    }
    // ===========================================

    public enum SearchType {
        CATEGORY, TEXT
    }
}