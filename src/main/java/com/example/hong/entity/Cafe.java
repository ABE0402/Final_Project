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
@Table(name = "cafes",
        indexes = {
                @Index(name = "idx_cafes_owner", columnList = "owner_user_id"),
                @Index(name = "idx_cafes_status_visible", columnList = "approval_status,is_visible"),
                @Index(name = "idx_cafes_geo", columnList = "lat,lng")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자를 필요로 합니다.

public class Cafe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // User 엔티티와 다대일 관계
    @JoinColumn(name = "owner_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cafes_owner"))
    private User owner; // owner_user_id 컬럼을 User 객체로 매핑   // 이거 삭제 예정

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(name = "address_road", length = 255, nullable = false)
    private String addressRoad;

    @Column(name = "business_number", length = 20)
    private String businessNumber;

    @Column(length = 10)
    private String postcode;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat; // 위도

    @Column(precision = 10, scale = 7)
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
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "favorites_count", nullable = false)
    @ColumnDefault("0")
    private int favoritesCount;

    @Enumerated(EnumType.STRING) // ENUM 타입을 문자열로 저장
    @Column(name = "approval_status", nullable = false)
    @ColumnDefault("'PENDING'")
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

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

    @Column(name = "operating_hours", columnDefinition = "TEXT")
    private String operatingHours;

    /** 메뉴 텍스트(자유형식: 메뉴명/가격 등) */
    @Column(name = "menu_text", columnDefinition = "TEXT")
    private String menuText;

    /** 메뉴 이미지(최대 5장, 단순 컬럼) */
    @Column(name = "menu_image_url1", length = 500) private String menuImageUrl1;
    @Column(name = "menu_image_url2", length = 500) private String menuImageUrl2;
    @Column(name = "menu_image_url3", length = 500) private String menuImageUrl3;
    @Column(name = "menu_image_url4", length = 500) private String menuImageUrl4;
    @Column(name = "menu_image_url5", length = 500) private String menuImageUrl5;


    // Cafe ↔ CafeTag (1:N)
    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CafeTag> cafeTags = new ArrayList<>();

    // [통합됨] 첫 번째 코드에 있던 CafeMenu 와의 연관관계 추가
    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CafeMenu> menus = new ArrayList<>();

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
        this.reviewCount = 0;
        this.averageRating = BigDecimal.ZERO;
        this.favoritesCount = 0;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.isVisible = true;
    }

    public void addTag(Tag tag) {
        CafeTag ct = CafeTag.of(this, tag);
        this.cafeTags.add(ct);
    }
    public void removeTag(Tag tag) {
        this.cafeTags.removeIf(ct -> ct.getTag().getId().equals(tag.getId()));
    }


}
