package com.example.hong.entity;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.domain.StoreType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="owner_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private Long userId;       // users.id

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=12)
    private StoreType storeType;                       // CAFE / RESTAURANT

    @Column(nullable=false, length=100) private String storeName;
    @Column(nullable=false, length=20)  private String businessNumber; // 사업자번호
    @Column(nullable=false, length=50)  private String ownerRealName;  // 본명
    @Column(nullable=false, length=20)  private String contactPhone;
    @Column(nullable=false, length=200) private String storeAddress;   // 간단주소(도로명)

    @Enumerated(EnumType.STRING)
    @Column( nullable=false, length=10)
    private ApprovalStatus status; // PENDING/APPROVED/REJECTED


    @Column(columnDefinition="TEXT") private String rejectionReason;
    private Long reviewedBy;                 // admin user id
    private LocalDateTime reviewedAt;

    @Column(nullable=false) private LocalDateTime createdAt;
    @Column(nullable=false) private LocalDateTime updatedAt;
}
