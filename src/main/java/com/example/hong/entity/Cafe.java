package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cafes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자를 필요로 합니다.

public class Cafe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // User 엔티티와 다대일 관계
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner; // owner_user_id 컬럼을 User 객체로 매핑   // 이거 삭제 예정

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(name = "address_road", length = 255, nullable = false)
    private String addressRoad;

    @Column(length = 10)
    private String postcode;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal lat; // 위도

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal lng; // 경도

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "hero_image_url", length = 500)
    private String heroImageUrl;

    @Column(name = "review_count", nullable = false)
    @ColumnDefault("0")
    private int reviewCount;

    @Column(name = "average_rating", precision = 3, scale = 2, nullable = false)
    @ColumnDefault("0.00")
    private BigDecimal averageRating;

    @Column(name = "favorites_count", nullable = false)
    @ColumnDefault("0")
    private int favoritesCount;

    @Enumerated(EnumType.STRING) // ENUM 타입을 문자열로 저장
    @Column(name = "approval_status", nullable = false)
    @ColumnDefault("'PENDING'")
    private ApprovalStatus approvalStatus;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "is_visible", nullable = false)
    @ColumnDefault("1")
    private boolean isVisible;

    @CreationTimestamp // 엔티티 생성 시각 자동 저장
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티 수정 시각 자동 저장
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Cafe 입장에서 자신을 참조하는 CafeTag 목록
    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CafeTag> cafeTags = new ArrayList<>();

    @Builder
    public Cafe(User owner, String name, String phone, String addressRoad, String postcode, String description, BigDecimal lat, BigDecimal lng) {
        this.owner = owner;
        this.name = name;
        this.phone = phone;
        this.addressRoad = addressRoad;
        this.postcode = postcode;
        this.lat = lat;
        this.lng = lng;
        this.description = description;

        // 엔티티 생성 시 기본값 설정
        this.reviewCount = 0;
        this.averageRating = BigDecimal.ZERO;
        this.favoritesCount = 0;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.isVisible = true;
    }

    // ApprovalStatus ENUM 정의
    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }

    // 연관관계 편의 메서드 (필요 시)
//    public void setOwner(User owner) {
//        this.owner = owner;
//        // owner.getCafes().add(this); // User 엔티티에 카페 목록이 있다면 양방향 관계 설정
//    }
}
