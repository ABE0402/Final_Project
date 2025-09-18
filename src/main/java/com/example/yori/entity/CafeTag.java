package com.example.yori.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "Cafe_Tags") // DDL에 명시된 테이블 이름
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(CafeTag.CafeTagId.class) // 복합키 클래스 지정
public class CafeTag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    // --- 복합키 클래스 ---
    public static class CafeTagId implements Serializable {
        private Long cafe;
        private Integer tag;
    }
}