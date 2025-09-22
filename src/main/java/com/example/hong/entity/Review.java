package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(name="uk_review_user_cafe", columnNames = {"user_id","cafe_id"}),
        indexes = {
                @Index(name="idx_reviews_cafe", columnList = "cafe_id"),
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name="cafe_id")
    private Cafe cafe;

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
