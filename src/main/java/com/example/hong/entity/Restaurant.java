package com.example.hong.entity;

import com.example.hong.domain.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants",
        indexes = {
                @Index(name = "idx_restaurants_owner", columnList = "owner_user_id"),
                @Index(name = "idx_restaurants_status_visible", columnList = "approval_status,is_visible"),
                @Index(name = "idx_restaurants_geo", columnList = "lat,lng"),
                @Index(name = "idx_restaurants_rating_review", columnList = "average_rating,review_count"),
                @Index(name = "idx_restaurants_favorites", columnList = "favorites_count")
        })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 점주(OWNER) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_restaurants_owner"))
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(name = "address_road", length = 255, nullable = false)
    private String addressRoad;

    @Column(length = 10)
    private String postcode;

    /** 위치정보(선택값) — null 허용 */
    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
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
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "favorites_count", nullable = false)
    @ColumnDefault("0")
    private int favoritesCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    @ColumnDefault("'PENDING'")
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "is_visible", nullable = false)
    @ColumnDefault("1")
    private boolean isVisible = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 태그 연결 (양방향) */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantTag> restaurantTags = new ArrayList<>();

    @Builder
    public Restaurant(User owner, String name, String phone, String addressRoad, String postcode,
                      String description, BigDecimal lat, BigDecimal lng) {
        this.owner = owner;
        this.name = name;
        this.phone = phone;
        this.addressRoad = addressRoad;
        this.postcode = postcode;
        this.description = description;
        this.lat = lat;
        this.lng = lng;

        this.reviewCount = 0;
        this.averageRating = BigDecimal.ZERO;
        this.favoritesCount = 0;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.isVisible = true;
    }

    /* 편의 메서드 (선택) */
    public void addTag(Tag tag) {
        RestaurantTag rt = RestaurantTag.of(this, tag);
        this.restaurantTags.add(rt);
    }
    public void removeTag(Tag tag) {
        this.restaurantTags.removeIf(rt -> rt.getTag().getId().equals(tag.getId()));
    }
}