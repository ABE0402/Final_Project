package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "Search_Event_Selections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(SearchEventSelection.SearchEventSelectionId.class) // 복합키 클래스 지정
public class SearchEventSelection {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_event_id")
    private SearchEvent searchEvent;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    // --- 아래 생성자를 추가해주세요 ---
    public SearchEventSelection(SearchEvent searchEvent, Tag tag) {
        this.searchEvent = searchEvent;
        this.tag = tag;
    }

    // --- 복합키 클래스 ---
    public static class SearchEventSelectionId implements Serializable {
        private Long searchEvent;
        private Integer tag;
    }
}