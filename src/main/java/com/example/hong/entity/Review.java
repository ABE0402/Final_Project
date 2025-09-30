package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_review_user_cafe", columnNames = {"user_id","cafe_id"}),
                @UniqueConstraint(name="uk_review_user_restaurant",  columnNames = {"user_id","restaurant_id"})
        },
indexes = {
                @Index(name="idx_reviews_cafe", columnList = "cafe_id"),
                @Index(name="idx_reviews_restaurant", columnList = "restaurant_id"),
                @Index(name="idx_reviews_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="cafe_id")
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(nullable = false)  // 1~5
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500) private String imageUrl1;
    @Column(length = 500) private String imageUrl2;
    @Column(length = 500) private String imageUrl3;
    @Column(length = 500) private String imageUrl4;
    @Column(length = 500) private String imageUrl5;

    @Column(nullable = false) @Builder.Default
    private boolean deleted = false;

    // 리뷰 평가 ai 모델 점수
    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY)
    private List<ReviewAspectScore> reviewAspectScores = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void validateTarget() {
        boolean hasCafe = this.cafe != null;
        boolean hasRest = this.restaurant != null;
        if (hasCafe == hasRest) {
            throw new IllegalStateException("리뷰 대상은 카페 또는 레스토랑 중 하나만 선택해야 합니다.");
        }
    }
}
