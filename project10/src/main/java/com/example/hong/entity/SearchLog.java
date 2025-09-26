package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_log",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_keyword", columnNames = {"user_id", "keyword"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 검색한 사용자

    @Column(nullable = false)
    private String keyword;

    @Column(name = "search_count", nullable = false)
    private int searchCount;

    @Column(name = "last_searched", nullable = false)
    private LocalDateTime lastSearched;
}