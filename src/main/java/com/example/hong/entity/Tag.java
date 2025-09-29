package com.example.hong.entity;

import com.example.hong.domain.TagAppliesTo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Table(
        name = "tags",
        uniqueConstraints = {
                // 동일 카테고리 내 이름은 유니크 (권장). 기존이 전역 unique였다면 그대로 unique=true만 유지해도 됨.
                @UniqueConstraint(name = "uk_tags_category_name", columnNames = {"category", "name"})
        },
        indexes = {
                @Index(name = "idx_tags_category", columnList = "category"),
                @Index(name = "idx_tags_applies_to", columnList = "applies_to"),
                @Index(name = "idx_tags_display_order", columnList = "display_order")
        }
)
@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 동반인/분위기/편의/예약/우선순위/종류 등 */
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /** 이 태그가 적용될 대상 (카페/식당/공용) */
    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to", nullable = false, length = 20)
    @Builder.Default
    private TagAppliesTo appliesTo = TagAppliesTo.BOTH;

    /** UI 정렬 등에 쓰는 가중치(낮을수록 먼저) */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Tag(String name, String category) { this.name=name; this.category=category; }

    public static Tag of(String name, String category) {
        Tag t = new Tag();
        t.name = name;
        t.category = category;
        return t;
    }
}