package com.example.hong.entity;

import com.example.hong.domain.AccountStatus;
import com.example.hong.domain.Gender;
import com.example.hong.domain.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password; // 폼 로그인 시 사용, 소셜만 쓰면 null 가능


    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30, unique = true)  // 활동명(닉네임)
    private String nickname;

    @Column(length = 20)
    private String phone;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Gender gender = Gender.UNKNOWN;

    @Column(nullable = true)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(length = 500, nullable = true)
    private String profileImageUrl;        // 예: /uploads/profiles/uuid.png

    @Column(columnDefinition = "TEXT", nullable = true)
    private String bio;                    // 자기소개

    /* ===== 추가: 생성/수정 시각 ===== */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* JPA 라이프사이클로 값 보정 (MySQL 기본값에 의존 X) */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

}
