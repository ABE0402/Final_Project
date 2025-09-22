package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_identities",
        uniqueConstraints = @UniqueConstraint(name = "uq_provider_subject", columnNames = {"provider","providerUserId"}),
        indexes = @Index(name = "idx_auth_user", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthIdentity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 10)
//    private AuthProvider provider; // KAKAO/GOOGLE/NAVER/APPLE/LOCAL


    @Column(name = "providerUserId", nullable = false, length = 255)
    private String providerUserId; // 공급자 측 사용자 식별자


    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime connectedAt = LocalDateTime.now();
}
