package com.example.hong.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "review_aspect_scores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewAspectScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(nullable = false, length = 50)
    private String aspect;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal score;
}