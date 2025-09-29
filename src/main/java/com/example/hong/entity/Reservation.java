package com.example.hong.entity;

import com.example.hong.domain.ReservationStatus;
import com.example.hong.domain.ReserveTargetType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations",
        indexes = {
                @Index(name = "idx_res_user_time", columnList = "user_id,reservation_at"),
                @Index(name = "idx_res_target", columnList = "targetType,targetId"),
                @Index(name = "idx_res_status", columnList = "status")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReserveTargetType targetType; // CAFE or RESTAURANT

    @NotNull
    private Long targetId;                // 대상 PK (Cafe.id or Restaurant.id)

    @NotNull
    @Column(name = "reservation_at")
    private LocalDateTime reservationAt;

    @Min(1)
    private int partySize;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @CreatedDate @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
