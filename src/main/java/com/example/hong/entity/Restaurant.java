package com.example.hong.entity;

import com.example.hong.domain.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurants",
        indexes = {
                @Index(name = "idx_restaurants_status_visible", columnList = "approvalStatus,isVisible"),
                @Index(name = "idx_restaurants_geo", columnList = "lat,lng")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 100)
    private String name;


    @Column(length = 255)
    private String addressRoad;


    @Column(length = 500)
    private String heroImageUrl;


    private double averageRating;
    private int reviewCount;


    private double lat;
    private double lng;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;


    @Column(nullable = false)
    @Builder.Default
    private boolean isVisible = true;
}