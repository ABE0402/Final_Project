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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cafe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(name = "address_road", length = 255, nullable = false)
    private String addressRoad;

    @Column(length = 10)
    private String postcode;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal lng;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    @ColumnDefault("'PENDING'")
    private ApprovalStatus approvalStatus;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "is_visible", nullable = false)
    @ColumnDefault("1")
    private boolean isVisible;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ✅ Cafe ↔ CafeTag (1:N)
    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CafeTag> cafeTags = new ArrayList<>();

    // ✅ Cafe ↔ CafeMenu (1:N)
    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CafeMenu> menus = new ArrayList<>();

    @Builder
    //나중에 User owner -> Long ownerUserId 변경
    public Cafe(User owner, String name, String phone, String addressRoad,
                String postcode, String description, BigDecimal lat, BigDecimal lng) {
        this.owner = owner;
        //this.ownerUserId = ownerUserId;
        this.name = name;
        this.phone = phone;
        this.addressRoad = addressRoad;
        this.postcode = postcode;
        this.lat = lat;
        this.lng = lng;
        this.description = description;

        this.reviewCount = 0;
        this.averageRating = BigDecimal.ZERO;
        this.favoritesCount = 0;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.isVisible = true;
    }

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
}
