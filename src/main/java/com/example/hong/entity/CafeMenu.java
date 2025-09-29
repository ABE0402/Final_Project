package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cafe_menus")
@Getter
@Setter
public class CafeMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer price;

    // ✅ N:1 관계 (여러 메뉴가 한 카페에 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;
}
