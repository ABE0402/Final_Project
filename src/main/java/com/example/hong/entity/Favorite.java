package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = @UniqueConstraint(name="uk_fav_user_cafe", columnNames = {"user_id","cafe_id"}),
        indexes = {
                @Index(name="idx_fav_cafe", columnList = "cafe_id"),
                @Index(name="idx_fav_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name="cafe_id")
    private Cafe cafe;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}